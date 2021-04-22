import javax.crypto.Mac
import org.apache.commons.codec.binary.Hex
import javax.crypto.spec.SecretKeySpec
import groovy.json.*
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*

String apiId = hostProps.get("lmaccess.id")
String apiKey = hostProps.get("lmaccess.key")
def portalName = hostProps.get("lmaccount")
String endPoint = "/functions"
String appliesTo = "auto.endpoint.model =~\"DCS-7060CX2-32S|DCS-7010T-48|DCS-7050SX|DCS-7050TX|DCS-7504|DCS-7508\" && auto.entphysical.softwarerev == \"4.22.7.1M\""

def map = [:]
map["type"] = "testAppliesTo"
map["originalAppliesTo"] = appliesTo
map["currentAppliesTo"] = appliesTo
map["needInheritProps"] = true

def jsonBody = new JsonBuilder(map).toString()

def generateAuth(id, key, path, data) {
    Long epoch_time = System.currentTimeMillis()    // Get current system time (epoch time)
    Mac hmac = Mac.getInstance("HmacSHA256")
    hmac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"))

    def signature = Hex.encodeHexString(hmac.doFinal("POST${epoch_time}${data}${path}".getBytes())).bytes.encodeBase64()

    return "LMv1 ${id}:${signature}:${epoch_time}"
}




//build HTTP POST
def url = "https://${portalName}.logicmonitor.com/santaba/rest${endPoint}"
def post = new HttpPost(url)
def auth = generateAuth(apiId,apiKey,endPoint,jsonBody)

post.addHeader("Authorization",auth)
post.addHeader("Content-Type", "application/json")
post.addHeader("X-Version", "3")
post.setEntity(new StringEntity(jsonBody))

//execute
def client = HttpClientBuilder.create().build()
def response = client.execute(post)

def bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
def jsonResponse = bufferedReader.getText()
println "response: \n" + jsonResponse["originalMatches"].size()
def slurper = new JsonSlurper()
def resultMap = slurper.parseText(jsonResponse)

return 0



