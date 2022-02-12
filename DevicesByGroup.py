from inspect import signature
import requests
import json
import hashlib
import base64
import time
import hmac

#Account info
Company = input("Enter your portal subdomain: ")
AccessId = input("Enter your API access ID: ")
AccessKey = input("Enter your API access key: ")

parent_group = {'name': 'Device by Type','parentId': 1}

child_groups = [
        {
            'name': "Network",
            'appliesTo': "isNetwork()"
        },
        {
            'name': "Linux Servers",
            'appliesTo': "isLinux()"
        },
        {
            'name': "Windows Servers",
            'appliesTo': "isWindows()"
        },
        {
            'name': "VMware Hosts",
            'appliesTo': "system.virtualization =~ \"VMware ESX host\""
        },
        {
            'name': "VMware vCenters",
            'appliesTo': "system.virtualization =~ \"VMware ESX vcenter\""
        },
        {
            'name': "XenServer",
            'appliesTo': "system.virtualization =~ \"Xen\""
        },
        {
            'name': "Hyper-V",
            'appliesTo': "hasCategory(\"HyperV\")"
        },
        {
            'name': "NetApp",
            'appliesTo': "isNetApp()"
        },
        {
            'name': "EMC",
            'appliesTo': "hasCategory(\"EMC\")"
        },
        {
            'name': "SQL Servers",
            'appliesTo': "hasCategory(\"MSSQL\")"
        },
        {
            'name': "Collectors",
            'appliesTo': "isCollectorDevice()"
        },
        {
            'name': "Misc",
            'appliesTo': "isMisc()"
        }
]

#request info
httpVerb = 'POST'
resourcePath = '/device/groups'

#construct URL
url = 'https://{}.logicmonitor.com/santaba/rest{}'.format(Company,resourcePath)

#create parent group and retrieve id
def createParentGroup(group_info):

    data = json.dumps(group_info)
    epoch = str(int(time.time() *1000))

    requestVars = httpVerb + epoch + data + resourcePath
    #construct signature
    hmac1 = hmac.new(AccessKey.encode(),msg=requestVars.encode(),digestmod=hashlib.sha256).hexdigest()
    signature = base64.b64encode(hmac1.encode())
    #construct headers
    auth = 'LMv1 {}:{}:{}'.format(AccessId,signature.decode(),epoch)
    headers = {'Content-Type':'application/json','Authorization':auth}
    response = requests.post(url,data=data,headers=headers)
    
    response_data = response.json()
    print(response_data)
    return response_data['data']['id']

def createChildGroups(groups):
    
    parent_id = createParentGroup(parent_group)
    for group in groups:
        data = json.dumps({'name':group["name"],'appliesTo':group['appliesTo'],'parentId':str(parent_id)})
        epoch = str(int(time.time() *1000))
        requestVars = httpVerb + epoch + data + resourcePath
        #construct signature
        hmac1 = hmac.new(AccessKey.encode(),msg=requestVars.encode(),digestmod=hashlib.sha256).hexdigest()
        signature = base64.b64encode(hmac1.encode())
        #construct headers
        auth = 'LMv1 {}:{}:{}'.format(AccessId,signature.decode(),epoch)
        headers = {'Content-Type':'application/json','Authorization':auth}
        response = requests.post(url,data=data,headers=headers)



createChildGroups(child_groups)