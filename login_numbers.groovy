import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import com.santaba.agent.groovyapi.http.*;
import groovy.json.JsonSlurper;

def hostName = java.net.URLEncoder.encode(hostProps.get("system.displayname"));

//Account Info
def accessId = hostProps.get("lmaccess.id");
def accessKey = hostProps.get("lmaccess.key");
def account = hostProps.get("lmaccount");
data = ''
def queryParams = '?fields=description,username,happenedOnLocal&filter=_all~signs+in&size=1000';
def resourcePath = "/setting/accesslogs";
def url = "https://" + account + ".logicmonitor.com" + "/santaba/rest" + resourcePath + queryParams;

def currentDate = new Date().format('yyyyMMdd')

//scriptCache for adding up logins in month
def scriptCache

//get current time
epoch = System.currentTimeMillis();

//calculate signature
requestVars = "GET" + epoch + resourcePath;

hmac = Mac.getInstance("HmacSHA256");
secret = new SecretKeySpec(accessKey.getBytes(), "HmacSHA256");
hmac.init(secret);
hmac_signed = Hex.encodeHexString(hmac.doFinal(requestVars.getBytes()));
signature = hmac_signed.bytes.encodeBase64();

// HTTP Get
CloseableHttpClient httpclient = HttpClients.createDefault();
httpGet = new HttpGet(url);
httpGet.addHeader("Authorization" , "LMv1 " + accessId + ":" + signature + ":" + epoch);
httpGet.addHeader("X-Version","2")
try {
  response = httpclient.execute(httpGet);
  responseBody = EntityUtils.toString(response.getEntity());
  code = response.getStatusLine().getStatusCode();

} catch (Exception err) {
  println("ERROR: Script failed while attempting to query $url API endpoint...\n${err?.message}")
}
response = httpclient.execute(httpGet);
responseBody = EntityUtils.toString(response.getEntity());
code = response.getStatusLine().getStatusCode();

// user groovy slurper
json_slurper = new JsonSlurper();
response_obj = json_slurper.parseText(responseBody);

def currentDate = new Date().format('yyyyMMdd')
count = 0

for(entry in response_obj.data.items) {
  date = (Date.parse("yyyy-MM-dd hh:mm:ss","$entry.happenedOnLocal").format('yyyyMMdd'));
    if (date == currentDate){
        count ++
    }
}

//print output
println "Logins=" + count
httpclient.close();

return (0);