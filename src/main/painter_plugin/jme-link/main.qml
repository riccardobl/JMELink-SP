import QtQuick 2.2
import Painter 1.0

PainterPlugin {
        property bool txUpdate: false
        property bool autoUpdate: false
        property var sendMapsButton: null
        property var autoLinkButton: null
        function texturesChanged(){
                if(txUpdate){
                        txUpdate=false;
                        return true;
                }
                return false;
        }       
        jsonServerPort: 6403 
        Component.onCompleted: {
                sendMapsButton = alg.ui.addToolBarWidget("ButtonSendMaps.qml");
                sendMapsButton.clicked.connect(function(){
                        alg.log.info("Send to jme");
                        txUpdate=true;       
                });

                autoLinkButton = alg.ui.addToolBarWidget("ButtonAutoLink.qml");
                autoLinkButton.clicked.connect(function(){
                        autoUpdate=!autoUpdate;       
                        alg.log.info((autoUpdate?"Enable":"Disable")+" jme link autoupdate");
                });
        }

        onTick: {

        }

        onConfigure: {

        }

        onApplicationStarted: {

        }

        onNewProjectCreated: {
                
        }

        onProjectOpened: {
              
        }

        onProjectAboutToClose: {
             
        }

        onProjectAboutToSave: {
   
        }

        onProjectSaved: {
         
        }

        onComputationStatusChanged: {
                if(autoUpdate) txUpdate=true;
        }
}