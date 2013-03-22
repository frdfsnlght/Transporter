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

    var markers = {};
    var gates = [];
    var infoPopup = false;
    var showAll = true;
    var showOnWorld = false;
    var showOffWorld = false;
    var showOffServer = false;

    var isGateVisible = function(gate) {
        if (gate.world !== dynmap.world.name) return false;
        if (showAll) return true;
        for (var i in gate.links) {
            var link = gate.links[i];
            var parts = link.split('.');
            if (parts.length == 3) {
                if (showOffServer) return true;
                continue;
            }
            if (parts.length == 2) {
                if (parts[0] === dynmap.world.name) {
                    if (showOnWorld) return true;
                } else {
                    if (showOffWorld) return true;
                }
                continue;
            }
        }
        return false;
    };

    var createGateInfo = function(gate) {
        var content = '<div class="transporterGateInfo"><img src="transporter/gate-info.png"/><p>';
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
        return content;
    };

    var rebuildMarkers = function() {
        $.each(markers, function(key, value) {
            markers[key].remove();
        });
        markers = {};
        for (var i in gates) {
            var gate = gates[i];
            var fullName = gate.world + '.' + gate.name;
            var position = dynmap.map.getProjection().fromWorldToLatLng(gate.x, gate.y, gate.z);
            markers[fullName] = new CustomMarker(position, dynmap.map, function(div) {
                var gate = this.gate;
                $(div).addClass('transporterGateMarker').hover(
                    function(eIn) {
                        if (infoPopup) infoPopup.remove();
                        infoPopup = $(createGateInfo(gate)).css({
                            top: $(div).css('top'),
                            left: $(div).css('left')
                        }).appendTo('.dynmap');
                    },
                    function(eOut) {
                        if (infoPopup) infoPopup.remove();
                        infoPopup = false;
                    }
                );
                this.toggle(isGateVisible(this.gate));
            });
            markers[fullName].gate = gate;
        }
    };

    var toggleMarkers = function() {
		$.each(markers, function(key, value) {
            var marker = markers[key];
            marker.toggle(isGateVisible(marker.gate));
	});
    };

    return {

        refresh: function() {
            $.getJSON('transporter/gates.json', function(newGates) {
                gates = newGates;
                rebuildMarkers();
            });
        },

        rebuild: function() {
            rebuildMarkers();
        },

        toggle: function() {
            toggleMarkers();
        },

        showAllGates: function(ctl, show) {
            showAll = show;
            toggleMarkers();
        },

        showOnWorldGates: function(ctl, show) {
            showOnWorld = show;
            toggleMarkers();
        },

        showOffWorldGates: function(ctl, show) {
            showOffWorld = show;
            toggleMarkers();
        },

        showOffServerGates: function(ctl, show) {
            showOffServer = show;
            toggleMarkers();
        }

    };
}();

componentconstructors['../transporter/transporter'] = function(dynmap, configuration) {

    // load our CSS
    var link = $("<link>");
    link.attr({
        type: 'text/css',
        rel: 'stylesheet',
        href: 'transporter/transporter.css'
    });
    $("head").append( link );

    // bind to events

    $(dynmap).bind('mapchanged', function() {
        transporter.rebuild();
    });

    // setup refresh
    setInterval(transporter.refresh, 60 * 1000);
    setTimeout(transporter.refresh, 5000);

}
