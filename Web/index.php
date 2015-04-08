<?php 

$conn = new PDO("mysql:host=localhost;dbname=braincamera","root","");


if 
(
	isset($_POST['id'])			&&
	isset($_POST['filename']) 	&&
	isset($_POST['image']) 		&&
	isset($_POST['latitude'])	&&
	isset($_POST['longitude'])
)
{
	// Retrieve the HTTP post variables
	$id =			$_POST['id'];
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
	$date = $_POST['datetime'];
	
	// Prepare a database query
	$sql = "INSERT INTO activity (Id, Datetime, Value) VALUES (:id,:date,:value)";
	
	// Query binding
	$query = $conn->prepare($sql);
	$query->execute(array(':id' => $id, ':date' => $date, ':value' => $value));

}

/* http://<domain>/index.php?func=get */
if(isset($_GET['func']) && $_GET['func'] == "get")
{
	$json = $conn->query("SELECT Id, Url, Latitude, Longitude FROM images")->fetchAll(PDO::FETCH_ASSOC);	
	$date = "";
	$dateStart = "";
	$dateEnd = "";
	$temp = "";
	$query = "";
	$mid = "";
	$index = 0;
	
	foreach($json as $array)
	{
		
		$date = substr($array["Url"], 7);
		$date = substr($date, 0, strlen($date) - 4);
		$date = date("Y-m-d H:i:s", strtotime($date));
		$dateStart = date("Y-m-d H:i:s", strtotime($date) - 3);
		$dateEnd = date("Y-m-d H:i:s", strtotime($date) + 3);
		$query = $conn->prepare("SELECT DISTINCT DATE_FORMAT(Datetime,'%S') AS DT, Id, Value, Datetime FROM activity WHERE Id = :id AND Datetime >= :datestart AND Datetime <= :dateend GROUP BY DT ORDER BY DT");	
		$query->execute(array(':id' => $array["Id"], ':datestart' => $dateStart, ':dateend' => $dateEnd));
		$temp = $query->fetchAll(PDO::FETCH_ASSOC);
		if(count($temp) > 1)
		{
			$mid = count($temp);
			if($mid % 2 == 0)
			{
				$mid = $mid - ($mid / 2);
			}
			else
			{
				$mid = (($mid - 1) / 2);
			}
			
			$json[$index]["Activity"] = floatval($temp[$mid]["Value"]);
			
		}
		else if(count($temp) == 1)
		{	
			$json[$index]["Activity"] = floatval($temp[0]["Value"]);
		}
		else
		{
			$json[$index]["Activity"] = 0;
		}
		
		$index++;
	}
	echo json_encode($json);
}

if(isset($_GET['func']) && $_GET['func'] == "brainwave/get" && isset($_GET['id']) && isset($_GET['datetime']))
{
	$dateStart = date("Y-m-d H:i:s", strtotime($_GET['datetime']) - 31);
	$dateEnd = date("Y-m-d H:i:s", strtotime($_GET['datetime']) + 31);
	$id = $_GET['id'];
	
	
	$query = $conn->prepare("SELECT DISTINCT DATE_FORMAT(Datetime,'%S') AS DT, Id, Value, Datetime FROM activity WHERE Id = :id AND Datetime >= :datestart AND Datetime <= :dateend GROUP BY DT ORDER BY DT LIMIT 11");
	$query->execute(array(':id' => $id, ':datestart' => $dateStart, ':dateend' => $dateEnd));
	$json = $query->fetchAll(PDO::FETCH_ASSOC);
	
	echo json_encode($json);
}

$conn = null;


?>