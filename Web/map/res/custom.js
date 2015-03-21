/*
 * Global variables
 */
var map;
var graphCategories = [];
var graphData = [];
var api = "http://localhost/index.php";

/*
 * addMarker(param) is responsible for adding
 * map points by utilizing the data that is sent
 * from the external calls.
 */
function addMarker(data)
{
	// Add a mapping point
	map.addMarker({
		lat: data.Latitude,
		lng: data.Longitude,
		title: data.Url,
		details: {
			database_id: data.Id
		},
		click: function(e){
			// Initialize the information panel
			initializeInfoPanel(data);
		}
		/*,
		mouseover: function(e){
			if(console.log)
				console.log(e);
		}
		*/
	});
}

/*
 * clearGraphData() clears the contents from the arrays
 */
function clearGraphData()
{
	graphCategories = [];
	graphData = [];
}

/*
 * drawGraph initializes the graph view by initializing and configuring
 * the contents of a specific graph container.
 */
function drawGraph()
{
	$('#graphContainer').highcharts({
        title: {
            text: 'Brainwave Activity',
            x: -20
        },
        xAxis: {
            categories: graphCategories
        },
        yAxis: {
            title: {
                text: ''
            },
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        tooltip: {
            valueSuffix: '%'
        },
        legend: {
            layout: 'vertical',
            align: 'right',
			enabled: false,
            verticalAlign: 'middle',
            borderWidth: 0
        },
        series: [{
            name: 'Activity',
            data: graphData
        }],
		credits: {
			  enabled: false
		}
    });
}

/*
 * populateGraphData(param,param) is responsible
 * for querying the API by performing an AJAX request
 */
function populateGraphData(id,date)
{
	$.ajax({                      
		type: "GET",
		url: api + '?func=brainwave/get&id=' + id +'&datetime=' + date,
		dataType: 'json',
		async:false,
		success: function(data){
			for(var i = 0; i < data.length; i++)
			{
				// Populate the arrays with the retrieved AJAX data
				graphCategories.push(data[i].Datetime);
				
				// Multiply with 100 to show real percentage values
				graphData.push(parseFloat(data[i].Value) * 100);
			}
		}
    });
}

/*
 * TODO - more implementation to follow
 */
function manipulateImageContainer(value)
{
	// Determine the change by brainwave value
	var change = 1 - value;
	
	// Apply invert filter
	$("#imgContainer img").css("-webkit-filter", "invert(" + change.toFixed(2) + ")");
}

/*
 * initializeInfoPanel(param) initializes the
 * information panel, this is called by the click
 * listener for a specific mapping point
 */
function initializeInfoPanel(data)
{
	// Place the image as the contents of the div
	$("#imgContainer").html('<img src="../' + data.Url + '" />');
	
	// Extract the datetime from the image URL
	var formattedDate = data.Url.substring(7,data.Url.length - 4);
	
	// Populate the graph by calling an external method
	// which performs an AJAX request
	populateGraphData(data.Id,formattedDate);
	
	// Draw the graph
	drawGraph();

	// Apply the image filter based on the brainwave value
	manipulateImageContainer(graphData[5] / 100);
	
	// Show the information container
	$("#infoContainer").show();
		
	// Show and animate the information panel
	$("#infoWindow").show("slow");
	
}


/*
 * populateMap() populates the Google Maps view
 * with points - it performs an AJAX request to
 * populate the map dynamically
 */
function populateMap()
{
	$.ajax({                      
		type: "GET",
		url: api + '?func=get',
		dataType: 'json',
		async: false,
		success: function(data){
			for(var i = 0; i < data.length; i++)
			{
				addMarker(data[i]);
			}
		}
    });
}

$(document).ready(function(){
   /*
    * Initialize a Google Maps view that covers the entire screen surface
	* Furthermore, declare starting position for the map.
	*/
	map = new GMaps({
		el: '#map',
		lat: 55.61545659999999,
		lng: 12.98404770000002,
		zoom: 10,
		mapTypeId: google.maps.MapTypeId.SATELLITE
	});
	
	// Populate the map with mapping points
	// It performs a simple AJAX request to 
	// retrieve data
	populateMap();

	// Fit zoom
	map.fitZoom();
	
   /*
	* Click listener for the cross sign to close the information panel
	*/
	$("#closeInfoWindow").click(function(){
		$("#infoWindow").hide("slow", function(){
			$("#infoContainer").hide();
			clearGraphData();
			$("#imgContainer img").css("-webkit-filter", "invert(0)");
		});
	});
	
   /*
	* Click listener for the information container
	*/
	$("#infoContainer").click(function(){
		// Simulate a click
		$("#closeInfoWindow").click();
	});
	
   /*
	* Click listener for the ESC-button to simulate a click to close
	* the information panel
	*/
	$(document).keyup(function(e){
		// ESC
		if(e.which == 27)
		{
			// Simulate a click
			$("#closeInfoWindow").click();
		}
	});
	
});