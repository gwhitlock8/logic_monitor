import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex;import com.santaba.agent.groovyapi.http.*
import groovy.json.JsonSlurper
//Account Info

Map credentials = [
    "paypal": [
        "id": hostProps.get("lmaccess.id"),
        "key": hostProps.get("lmaccess.key"),
        "account": hostProps.get("lmaccount")
    ],
    "corp": [
        "id": hostProps.get("corp.lmaccess.id"),
        "key": hostProps.get("corp.lmaccess.key"),
        "account": hostProps.get("corp.lmaccount")
    ],
    "lb": [
        "id": hostProps.get("lb.lmaccess.id"),
        "key": hostProps.get("lb.lmaccess.key"),
        "account": hostProps.get("lb.lmaccount")
    ]
]

Map data = [
    "paypal": [],
    "corp":[],
    "lb":[]
]

def resourcePath = "/metrics/usage"


//define API endpoint path

credentials.each{ entry -> 
    
    url = "https://" + entry.value.account + ".logicmonitor.com" + "/santaba/rest" + resourcePath

    //get current time
    epoch = System.currentTimeMillis()

    //calculate signature
    requestVars = "GET" + epoch + resourcePath
    
    hmac = Mac.getInstance("HmacSHA256");
    secret = new SecretKeySpec((entry.value.key).getBytes(), "HmacSHA256");
    hmac.init(secret);
    hmac_signed = Hex.encodeHexString(hmac.doFinal(requestVars.getBytes()));
    signature = hmac_signed.bytes.encodeBase64();


    // HTTP Get
    CloseableHttpClient httpclient = HttpClients.createDefault();
    httpGet = new HttpGet(url);
    httpGet.addHeader("Authorization" , "LMv1 " + entry.value.id + ":" + signature + ":" + epoch);
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
            
    json_slurper = new JsonSlurper();
    response_obj = json_slurper.parseText(responseBody);
    println response_obj

    httpclient.close()

    
}


return 0 //Exit Successfully




