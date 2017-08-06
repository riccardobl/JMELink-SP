import AlgWidgets.Style 1.0
import QtQuick 2.7
import QtQuick.Controls 1.4
import QtQuick.Controls.Styles 1.4

Button {
  id:root
  height: 30
  width: height
  tooltip: "<p>Enable/disable autoupdate</p>"

  property bool enableAutoLink: false

  style: ButtonStyle {
    background: Rectangle {
        color: (root.enabled && hovered)? AlgStyle.background.color.gray : "#141414"
    }
    label: Item {
      height: control.height
      width: control.width
      Rectangle {
        anchors.fill: parent
        anchors.margins: 2
        radius: width
        color: !root.enabled? "#666" : (enableAutoLink?
          (hovered? "#0d0" : "#0a0") :
          (hovered? "#d00" : "#a00")
        )
      }
    }
  }

  onClicked: {
    if (root.enabled) {
      enableAutoLink = !enableAutoLink
    }
  }
}
