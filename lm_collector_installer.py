#!/usr/bin/env python3

from __future__ import print_function
import logicmonitor_sdk
from logicmonitor_sdk.rest import ApiException
from pprint import pprint
import os, stat

# Configure API key authorization: LMv1
configuration = logicmonitor_sdk.Configuration()
configuration.company = 'lmgavinwhitlock'
configuration.access_id = '9CGE866Pg5yZ27mY3Wa4'
configuration.access_key = '47-6F8^Gjz32Rz+zwkr)32YJqDW{b)p~VXqdC~V='

# create an instance of the API class
api_instance = logicmonitor_sdk.LMApi(logicmonitor_sdk.ApiClient(configuration))
    

def collectorInstaller(newCollectorId):
    
    collectorId = newCollectorId
    osAndArch='Linux64' 
    collectorSize='nano'
    collectorVersion = 31003

    try:
        # get collector installer
        api_response = api_instance.get_collector_installer(collectorId, osAndArch, collector_size=collectorSize,collector_version=collectorVersion)
        pprint(api_response)
        with open("logicmonitorsetup.bin",'wb') as f:
            f.write(api_response.data)
        cmd = "./logicmonitorsetup.bin"
        os.chmod(cmd,755)
        os.system("./logicmonitorsetup.bin -y")
    except ApiException as e:
        print("Exception when calling LMApi->getCollectorInstaller: %s\n" % e)

def createNewCollector():
    body={"has_fail_over_device":False,"collector_group_id":0,"arch":"Linux64","hostname":"test_collector"}

    try:
        # add collector
        api_response = api_instance.add_collector(body)
        pprint(api_response.id)
    except ApiException as e:
        print("Exception when calling LMApi->addCollector: %s\n" % e)
    
    return api_response.id

collectorInstaller(createNewCollector())