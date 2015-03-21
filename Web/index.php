<?php 

$conn = new PDO("mysql:host=localhost;dbname=braincamera","root","");


if 
(
	isset($_POST['filename']) 	&&
	isset($_POST['image']) 		&&
	isset($_POST['latitude'])	&&
	isset($_POST['longitude'])
)
{
	// Retrieve the HTTP post variables
	$id =			"1";
	$filename = 	"images/" . $_POST['filename'];
	$image = 		$_POST['image'];
	$latitude = 	$_POST['latitude'];
	$longitude = 	$_POST['longitude'];
	
	// Prepare a database query	
	$sql = "INSERT INTO images (Id, Url, Latitude, Longitude) VALUES (:id,:filename,:latitude,:longitude)";
	
	// Query binding
	$query = $conn->prepare($sql);
	$query->execute(array(':id' => $id, ':filename' => $filename, ':latitude' => $latitude, ':longitude' => $longitude));

	
	$decoded=base64_decode($image);

	file_put_contents($filename,$decoded);
}

if(isset($_GET['func']) && $_GET['func'] == "add" && isset($_POST['value']) && isset($_POST['id']))
{
	// Retrieve the HTTP POST variables
	$id = $_POST['id'];
	$value = $_POST['value'];
	$date = date("Y-m-d H:i:s");
	
	// Prepare a database query
	$sql = "INSERT INTO activity (Id, Datetime, Value) VALUES (:id,:date,:value)";
	
	// Query binding
	$query = $conn->prepare($sql);
	$query->execute(array(':id' => $id, ':date' => $date, ':value' => $value));

}

/* http://<domain>/index.php?func=get */
if(isset($_GET['func']) && $_GET['func'] == "get")
{
	$json = $conn->query("SELECT * FROM images")->fetchAll(PDO::FETCH_ASSOC);	
	echo json_encode($json);
}

if(isset($_GET['func']) && $_GET['func'] == "brainwave/get" && isset($_GET['id']) && isset($_GET['datetime']))
{
	$dateStart = date("Y-m-d H:i:s", strtotime($_GET['datetime']) - 5);
	$dateEnd = date("Y-m-d H:i:s", strtotime($_GET['datetime']) + 5);
	$id = $_GET['id'];
	
	
	$query = $conn->prepare("SELECT * FROM activity WHERE Id = :id AND Datetime >= :datestart AND Datetime <= :dateend ORDER BY Datetime");
	$query->execute(array(':id' => $id, ':datestart' => $dateStart, ':dateend' => $dateEnd));
	$json = $query->fetchAll(PDO::FETCH_ASSOC);
	
	echo json_encode($json);
}


?>