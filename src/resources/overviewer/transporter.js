/* 
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

transporter = function() {

    var markers = [];
    var showAll = false;
    var showOnWorld = false;
    var showOffWorld = false;
    var showOffServer = false;
    
    updateVisibleGates = function() {
        for (var i in markers) {
            var marker = markers[i];
            marker.setVisible(isGateVisible(marker.gate));
        }
    };
    
    isGateVisible = function(gate) {
        if (showAll) return true;
        for (var i in gate.links) {
            var link = gate.links[i];
            var parts = link.split('.');
            if (parts.length == 3) {
                if (showOffServer) return true;
                continue;
            }
            if (parts.length == 2) {
                if (parts[0] === transporterConfig.world) {
                    if (showOnWorld) return true;
                } else {
                    if (showOffWorld) return true;
                }
                continue;
            }
        }
        return false;
    };
    
    prepareGateInfo = function(marker) {
        var gate = marker.gate;
        var content = '<div class="infoWindow gateInfo"><img src="' +
                            transporterConfig.gateInfoIcon +
                        '"/><p>';
        content += 'Name: ' + gate.name + '<br/>';
        content += 'Design: ' + gate.design + '<br/>';
        content += 'Creator: ' + gate.creator + '<br/>';
        if (gate.onWorldSend || gate.onWorldReceive)
            content += 'On-world travel: ' + gate.onWorldSend + '/' + gate.onWorldReceive + '<br/>';
        if (gate.offWorldSend || gate.offWorldReceive)
            content += 'Off-world travel: ' + gate.offWorldSend + '/' + gate.offWorldReceive + '<br/>';
        if (gate.offServerSend || gate.offServerReceive)
            content += 'Off-server travel: ' + gate.offServerSend + '/' + gate.offServerReceive + '<br/>';

        content += '</p></div>';
        var infoWindow = new google.maps.InfoWindow({
            'content': content
        });
        google.maps.event.addListener(marker, 'click', function() {
            if (transporter.infoWindow)
                transporter.infoWindow.close();
            infoWindow.open(overviewer.map, marker);
            transporter.infoWindow = infoWindow;
        });
    };
    
    return {

        refresh: function() {
            for (var i in markers)
                markers[i].setMap(null);
            markers = [];
            $.getJSON(transporterConfig.gates, function(gates) {
                for (var i in gates) {
                    var gate = gates[i];
                    if (gate.world !== transporterConfig.world) continue;
                    var position = overviewer.util.fromWorldToLatLng(gate.x, gate.y, gate.z);
                    var marker = new google.maps.Marker({
                        position: position,
                        map: overviewer.map,
                        title: gate.name,
                        icon: transporterConfig.gateMarkerIcon,
                        visible: isGateVisible(gate)
                    });
                    marker.gate = gate;
                    markers.push(marker);
                    if (transporterConfig.gateInfo)
                        prepareGateInfo(marker);
                }
            });
        },
        
        showAllGates: function(ctl, show) {
            showAll = show;
            updateVisibleGates();
        },
        
        showOnWorldGates: function(ctl, show) {
            showOnWorld = show;
            updateVisibleGates();
        },
        
        showOffWorldGates: function(ctl, show) {
            showOffWorld = show;
            updateVisibleGates();
        },
        
        showOffServerGates: function(ctl, show) {
            showOffServer = show;
            updateVisibleGates();
        }
        
    };
}();

if ((! transporterConfig) || (! $.isPlainObject(transporterConfig))) transporterConfig = {};
transporterConfig = $.extend({
    world: 'world',
    refresh: 60,
    gates: 'transporter/gates.json',
    gateMarkerIcon: 'transporter/gate-marker.png',
    gateInfo: true,
    gateInfoIcon: 'transporter/gate-info.png'
}, transporterConfig);

setInterval(transporter.refresh, 1000 * transporterConfig.refresh);
setTimeout(transporter.refresh, 1000);

if (typeof(menus) === 'object') {
    new menus.Menu({
        label: 'Transporter',
        items: [{
            type: 'checkbox',
            label: 'All gates',
            action: transporter.showAllGates
        }, {
            type: 'checkbox',
            label: 'On-world gates',
            action: transporter.showOnWorldGates
        }, {
            type: 'checkbox',
            label: 'Off-world gates',
            action: transporter.showOffWorldGates
        }, {
            type: 'checkbox',
            label: 'Off-server gates',
            action: transporter.showOffServerGates
        }]
    });
} else
    window.alert('The Transporter add-on requires the Menus add-on.');
