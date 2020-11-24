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
//get current time
//calculate signature
//define API endpoint path
if (credentials.paypal.id && credentials.corp.id && credentials.lb.id) {
    //loop through credentials to build URL for each respective portal
    credentials.each { p ->
        url = "https://" + p.value.account + ".logicmonitor.com" + "/santaba/rest" + resourcePath

        epoch = System.currentTimeMillis()
        requestVars = "GET" + epoch + resourcePath
        hmac = Mac.getInstance("HmacSHA256")
        secret = new SecretKeySpec((p.value.key).getBytes(), "HmacSHA256")
        hmac.init(secret)
        hmac_signed = Hex.encodeHexString(hmac.doFinal(requestVars.getBytes()))
        signature = hmac_signed.bytes.encodeBase64()

        //HTTP Get
        CloseableHttpClient httpclient = HttpClients.createDefault()
        httpGet = new HttpGet(url)
        httpGet.addHeader("Authorization","LMv1" + "$p.value.id" + ":" + signature + ":" + epoch)
        httpGet.addHeader("X-Version","2")
        try {
            response = httpclient.execute(httpGet)
            responseBody = EntityUtils.toString(response.getEntity())
            code = response.getStatusLine().getStatusCode()
            println(responseBody)
        } catch (Exception e){
            println(e, "ERROR????");return 1
        }
        
        //user groovy slurper
        if (code == "200"){
          json_slurper = new JsonSlurper()
          response_obj = json_slurper.parseText(responseBody)
          data[p.key] = response_obj
        } else {
          return code
        }
        httpclient.close()
    }
} else {
    println("Device is not configured with the necessary portal credentials to proceed with API queries.\n" +
            "Exiting Program...")
}
return 0 //Exit Successfully
data.each() {k, v -> println("$k=$v")}