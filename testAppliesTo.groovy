
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPut
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;



def apiId = hostProps.get("lmaccess.id")
def apiKey = hostProps.get("lmaccess.key")
def portalName = hostProps.get("lmaccount")

def resourcePath = "/functions"

def url = "https://" + account + ".logicmonitor.com" + "/santaba/rest" + resourcePath

def appliesTo = "\"auto.endpoint.model =~\\\"DCS-7060CX2-32S|DCS-7010T-48|DCS-7050SX|DCS-7050TX|DCS-7504|DCS-7508\\\" && auto.entphysical.softwarerev == \\\"4.22.7.1M\\\"\",\"currentAppliesTo\":\"auto.endpoint.model =~ \\\"DCS-7060CX2-32S|DCS-7010T-48|DCS-7050SX|DCS-7050TX|DCS-7504|DCS-7508\\\" && auto.entphysical.softwarerev == \\\"4.22.7.1M\\\"\""

def data = "{\"type\":\"testAppliesTo\",\"originalAppliesTo\":${appliesTo},\"needInheritProps\":true}";

epoch = System.currentTimeMillis(); //get current time

requestVars = "POST" + epoch + data + resourcePath;

// construct signature
hmac = Mac.getInstance("HmacSHA256");
secret = new SecretKeySpec(accessKey.getBytes(), "HmacSHA256");
hmac.init(secret);
hmac_signed = Hex.encodeHexString(hmac.doFinal(requestVars.getBytes()));
signature = hmac_signed.bytes.encodeBase64();

// HTTP PUT
CloseableHttpClient httpclient = HttpClients.createDefault();
http_request = new HttpPosturl);
http_request.setHeader("Authorization" , "LMv1 " + accessId + ":" + signature + ":" + epoch);
http_request.setHeader("Accept", "application/json");
http_request.setHeader("Content-type", "application/json");
http_requst.setHeader("X-Version","3");
http_request.setEntity(params);
response = httpclient.execute(http_request);
responseBody = EntityUtils.toString(response.getEntity());
code = response.getStatusLine().getStatusCode();

// Print Response
println "Status:" + code;
println "Response body:" + responseBody;
httpclient.close(); 