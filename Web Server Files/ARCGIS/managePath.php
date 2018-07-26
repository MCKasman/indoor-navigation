<?php
    // Include database configuration
    // include_once('dbconfig.php');
    $dbServer = 'aisdb521d.utdallas.edu:2443';
    $dbUsername = 'ecs_clark18_adm';
    $dbPassword = 'EC$Clark2018';
    $dbDatabase = 'ecs_clark18';

    $mysqli = mysqli_connect($dbServer, $dbUsername, $dbPassword, $dbDatabase) or die('Cannot connect to the database');
    $mysqli->set_charset("UTF8");

    $query = $mysqli->query("select * from Address");

    //Initiate the data array
    $data = array();

    if($query)
    {
        //$hold acts as a the table data catch
        while($hold = $query->fetch_array())
        {
            extract($hold);
            $data[] = array("userIP" => $userIP, "userXCoords" => $userXcoords, "userYCoords" => $userYcoords, "userZCoords" => $userZcoords);
        }
        $query->close();
    }

    //End connection to database
    @mysqli_close($mysqli);

    //Compile data for json encoding
    $result = array();
    $result["address"] = $data;

    /* // Output header; currently set for webpage as default
    header('Content-type: application/json');
    echo json_encode($result);
    */

    /* //push data from web server to user interface
    $_POST
    */
?>