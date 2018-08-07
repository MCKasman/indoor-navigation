<?php

$tokenFull = array();
/*
   * isJson checks if an input string is json.
   * Parameters:  $string, the variable to be tested
   * Return:      true if $string is json, false if not
   */
function isJson($string)
{
    json_decode($string);
    return (json_last_error() == JSON_ERROR_NONE);
}

/* getFloor returns the floor number from a CMX object
 * Parameters:  $string, the returned building and floor number from CMX
 * Return:      $floor, the separated floor number
 */
function getFloor($string)
{
    $floor = substr($string, strpos($string, ">") + 1);
    return $floor;
}


class RouteFunctions
{

    /* compilePath creates a usable coordinate array for the User Interface group to use.
     * Parameters:  $json, a raw json object directly from ArcGIS containing the coordinate locations for the path
     * Return:      $route, a 1 dimensional array containing points in the form (z1, x1, y1, z2, x2, y2,... zn, xn, yn)
     */
    public static function compilePath($json,$reverse)
    {

        //In case $json is empty
        if (empty($json)) {
            return "[]";
        }

        //Extract arrays from raw json
        $arcGISData = json_decode($json, TRUE);

        //print_r($arcGISData);

        //Initiate variables
        $route = array();
        $xCoordList = array();
        $yCoordList = array();
        $zCoordList = array();
        $counter = 0;

        //Construct coord lists from $arcGISData

        /*if (empty($argisData['routes'])) {
            return json_encode(array("route"=>array()));
        }
        else {*/
            foreach ($arcGISData['routes']['features'][0]['geometry']['paths'][0] as $item) {
                $xCoordList[$counter] = $item[0];
                $yCoordList[$counter] = $item[1];
                $zCoordList[$counter] = $item[2];
                $counter++;
            }
        //}
        //reverse the order if the path needs to  be greater roomno to lesser roomno

        if($reverse)
        {
            $xCoordList = array_reverse($xCoordList);
            $yCoordList = array_reverse($yCoordList);
            $zCoordList = array_reverse($zCoordList);
        }

        //Construct $route from $zCoordList, $xCoordList and $yCoordList
        //$route will be a 1d array containing [z1, x1, y1, z2, x2, y2,... zn, xn, yn]
        for ($counter = 0; $counter < count($xCoordList); $counter++) {
            $route[$counter * 3] = $zCoordList[$counter];
            $route[$counter * 3 + 1] = $xCoordList[$counter];
            $route[$counter * 3 + 2] = $yCoordList[$counter];
        }

        //print_r($route);

        //return compiled json encoded route
        return json_encode(array("route"=>$route));
    }

    public function ciscoXtoArc($x)
    {
      $xOrigin = 763744.56774118936;
      return ((((1934 / 241 * $x) / 108 * 32) * 0.3048) + $xOrigin);
    }

    public function ciscoYtoArc($y)
    {
      $yOrigin = 2147946.172976975;
      return ($yOrigin - (((933 / 160 * $y) / 108 * 32) * 0.3048));
    }

    public function parseCMXPoint($CMX)
    {
      $coord[0] = $this->ciscoXtoArc($CMX["response"]["mapCoordinate"]["x"]);
      $coord[1] = $this->ciscoYtoArc($CMX["response"]["mapCoordinate"]["y"]);
      $coord[2] = getFloor($CMX["response"]["mapInfo"]["mapHierarchy"]);

      //print_r($coord);

      return RouteFunctions::findNearest($coord[0],$coord[1],"floornum=" . $coord[2]);
    }

    public function queryRoomNum($roomnum)
    {
      $ch = curl_init();
      curl_setopt_array($ch,array(
        CURLOPT_RETURNTRANSFER => TRUE,
        CURLOPT_URL => 'https://cybernetics.utdallas.edu/server/rest/services/IndoorNav/GreenFinalNet/MapServer/6/query?' .
          http_build_query(array(
            "where" => "roomno='" . $roomnum . "'",
            "returnZ" => true,
            "f" => "json"
      ))));
      $resp = curl_exec($ch);
      curl_close($ch);

      $geometry = json_decode($resp,TRUE);

      $geometry = $geometry["features"]["0"]["geometry"];

      $zxy = json_encode(array($geometry["z"],$geometry["x"],$geometry["y"]));

      return $zxy;
    }

    /* getPath sends a request to the ArcGIS server to receive a raw json object for the path.
     * Parameters:  $CMX, the starting location in coordinate form (x, y, z), given through CMX. Should be an
     *                  associative array, although checks are made to test and decode possible json.
     *              $end, the ending destination in coordinate form (x, y, z)
     * Return:      $json, a raw json object containing arcGIS' output
     * Notes:       Because Cisco CMX, the software for getting the user location, cannot interface with
     *              ArcGIS, the tool to create the routes in the buildings, the live update feature is discontinued
     */
    public function getPath($CMX, $end, $stairs, $elevators)
    {
        $startCoord = array();
        $startTemp = array();
        $startRoom = '';
        $endCoord = array();
        $endTemp = array();
        $endRoom = '';

        //Test $CMX to see if it is an associative array, a raw json object, or a room number.
        //if (isJson($CMX) == TRUE) {
            //$this->$startTemp = json_decode($CMX, TRUE);
        //    echo "reeeeeeee";
        //}
        if (is_array($CMX)) {
            $startTemp = $CMX;
        }
        //If $CMX is a room number
        else if (is_string($CMX)){
            $startRoom = $CMX;
        }
        //If none/invalid
        else {
            return "Case 1";//"Error: Invalid input. Raw json, associative array, or room number required.";
        }

        //Test $end to see if it is an associative array, a raw json object, or a room number.
        //if (isJson($end) == TRUE) {
        //    $endTemp = json_decode($end, TRUE);
        //}
        if (is_array($end)) {
            $endTemp = $end;
        }
        //If $end is a room number
        else if (is_string($end)){
            $endRoom = $end ;
        }
        //If none/invalid
        else {
            return "Error: Invalid input. Raw json, associative array, or room number required.";
        }

        //Creates $startCoord, a coordinate in the (z, x, y) form if $CMX is not a room number
        if ($startRoom == '')
        {
            $this->startCoord[0] = $startTemp["response"]["mapCoordinate"]["x"];
            $this->startCoord[1] = $startTemp["response"]["mapCoordinate"]["y"];
            $this->startCoord[2] = getFloor($startTemp["response"]["mapInfo"]["mapHierarchy"]);
        }

        //Creates $endCoord, a coordinate in the (z, x, y) form if $end is not a room number
        if ($endRoom == '')
        {
            $this->endCoord[0] = $endTemp["response"]["mapCoordinate"]["x"];
            $this->endCoord[1] = $endTemp["response"]["mapCoordinate"]["y"];
            $this->endCoord[2] = getFloor($endTemp["response"]["mapInfo"]["mapHierarchy"]);
        }

        //If given a CMX coordinate array for the starting location
        if ($startRoom == ''){
            $x = $this->ciscoXtoArc($this->startCoord[0]);
            $y = $this->ciscoYtoArc($this->startCoord[1]);
            $floornum = $this->startCoord[2];
            return RouteFunctions::solveRoute($this->findNearest($x,$y,"floornum=" . $floornum), $endRoom, $stairs, $elevators);
        }
        //If given a room number for the starting location
        else
        {
            //echo "Room to room nav";
            return RouteFunctions::solveRoute($startRoom, $endRoom, $stairs, $elevators);
        }

    }

    /* generateToken creates a token request with the information needed to request a route.
     * Parameters:  $username, the username for creating a post request
     *              $password, the password for post requests
     *              $client, default page to work with (arcGIS route solver)
     *              $f, the format of the requests (pjson)
     * Returns:     $token, the generated token
     */
    static function generateToken()
    {
        // Setup cURL
        $ch = curl_init("https://cybernetics.utdallas.edu/portal/sharing/rest/generateToken");
        curl_setopt_array($ch, array(
            CURLOPT_POST => TRUE,
            CURLOPT_RETURNTRANSFER => TRUE,
            CURLOPT_HEADER => FALSE,
            CURLOPT_POSTFIELDS => array(
                "username" => "GAIALAB1",
                "password" => "g@ialab123",
                "client" => "referer",
                "referer" => "https://cybernetics.utdallas.edu/server/",
                "f" => "json"
            )
        ));

        // Send the request
        $response = curl_exec($ch);
        curl_close($ch);

        // Check for errors
        if($response === FALSE){
            die(curl_error($ch));
        }

        // Return the decoded response
        global $tokenFull;
        $tokenFull = json_decode($response, TRUE);
    }

    /* validateURL checks is an input URL is valid.
     * Parameters:  $url, the url to be tested
     * Return:      true is $url is a valid url, false if not
     */
    function validateURL($url = '')
    {
        if ($url = '')
        {
            return FALSE;
        }
        // Remove all illegal characters from a url
        $url = filter_var($url, FILTER_SANITIZE_URL);

        // Validate url
        if (filter_var($url, FILTER_VALIDATE_URL))
        {
            return TRUE;
        }
        else
        {
            return FALSE;
        }
    }

    /* findNearest finds the nearest arcGIS point on the arcGIS server map from the given parameters
     * Parameters:  $x, the x value of the point
     *              $y, the y value of the point
     *              $filter, any filters for specific point types (stairs, restrooms, etc)
     * Return:      $roomno, the room number that is closest to the x and y values, following filters
     */
    static function findNearest($x, $y, $filter = "1=1") {
        global $tokenFull;
        //Submit request for job ID.
        $ch = curl_init();
        curl_setopt_array($ch, array(
            CURLOPT_RETURNTRANSFER => TRUE,
            CURLOPT_URL => "https://cybernetics.utdallas.edu/server/rest/services/System/SpatialAnalysisTools/GPServer/FindNearest/submitJob?" .
                http_build_query (array(
                    "analysisLayer" => '{"layerDefinition":{"geometryType": "esriGeometryPoint","fields": []},"featureSet": {"geometryType": "esriGeometryPoint","spatialReference": {"wkid": 32138 },"features": [{"geometry": {"x": ' . $x . ',"y": ' . $y . '}}]}}',
                    "nearLayer" => '{"url":"https://cybernetics.utdallas.edu/server/rest/services/IndoorNav/GreenFinalNet/MapServer/6","filter":"' . $filter . '"}',
                    "f" => 'json',
                    "token" => $tokenFull['token'],
                    "maxCount" => 1,
                ))));
        $resp = curl_exec($ch);
        curl_close($ch);
        $jobID = json_decode($resp, TRUE);
        $jobID = $jobID['jobId'];


        /*
        1) submit request
        2) wait for asynchronous process, know when its done by requesting "jobStatus": esriJobSucceeded
            -jobsucceeded
            -if jobfailed, spit error
        3) parse response (value,featureSet,Features,attributes,roomno)
         */

        $ch = curl_init();
        curl_setopt_array($ch, array(
            CURLOPT_RETURNTRANSFER => TRUE,
            CURLOPT_URL => 'https://cybernetics.utdallas.edu/server/rest/services/System/SpatialAnalysisTools/GPServer/FindNearest/jobs/' . $jobID . '?token=' . $tokenFull['token'] . '&f=json',
        ));
        do {
            sleep(.1);
            $resp = curl_exec($ch);

            $jobStatus = json_decode($resp, TRUE);
            $jobStatus = $jobStatus['jobStatus'];
            if($jobStatus == "esriJobFailed"){
                echo "Error 0: JOB FAILED";
                break;
            }
        } while ($jobStatus != "esriJobSucceeded");
        curl_close($ch);


        $ch = curl_init();
        curl_setopt_array($ch, array(
            CURLOPT_RETURNTRANSFER => TRUE,
            CURLOPT_URL => 'https://cybernetics.utdallas.edu/server/rest/services/System/SpatialAnalysisTools/GPServer/FindNearest/jobs/' . $jobID . '/results/nearestLayer?token=' . $tokenFull['token'] . '&f=json',
        ));
        $resp = curl_exec($ch);
        curl_close($ch);
        $roomno = json_decode($resp, TRUE);
        $roomno = $roomno['value']['featureSet']['features']['0']['attributes']['roomno'];
        return $roomno;
    }

    /* solveRoute processes and returns a given route from the given parameters
     * Parameters:  $originroom, the starting room for the route
     *              $destinationroom, the ending room for the route
     *              $stairs, a boolean indicating if the user wants to use stairs
     *              $elevators, a boolean indicating if the user wants to use elevators
     * Return:      compilePath's return
     *
     *  Notes:  Do not use rooms with subsections in them (I.e. letters), as this negatively effects ArcGIS.
     *      This note was resolved as of July 26, 2018. In the event that paths become wildly off course,
     *      this error may be the cause. This is why the note is not deleted.
     */
    static function solveRoute($originroom, $destinationroom, $stairs, $elevators)
    {
        global $tokenFull;
        $reverse = false;
        $ch = curl_init();
        $barriers = '';

        $preferences = '';

        //echo "In solveRoute: $originroom\n$destinationroom\n$stairs\n$elevators";

        if($stairs == "false")//!stairs
        {
            $preferences = 'spacetype=\'Stairway\'';

            if($elevators == "false")//!elevators
            {
                $preferences = $preferences . ' OR spacetype=\'Elevator Cab\'';
            }
        }
        else if($elevators == "false")
        {
            $preferences = 'spacetype=\'Elevator Cab\'';
        }


        if($preferences != '')
        {
            //$preferences must be set to either "Stairway" or "Elevator Cab" to avoid  the corresponding vertical transport method
            $barriers = '{"type" : "features","url" : "https://cybernetics.utdallas.edu/server/rest/services/IndoorNav/GreenFinalNet/MapServer/7/query?where=' . $preferences . '&returnZ=true&f=json","doNotLocateOnRestrictedElements" : true}';
        }
        curl_setopt_array($ch, array(
            CURLOPT_RETURNTRANSFER => TRUE,
            CURLOPT_URL => 'https://cybernetics.utdallas.edu/server/rest/services/IndoorNav/GreenFinalNet/NAServer/Route/solve?' .
                http_build_query(array(
                    'stops' => '{"type" : "features","url" : "https://cybernetics.utdallas.edu/server/rest/services/IndoorNav/GreenFinalNet/MapServer/6/query?where=roomno+%3D+\'' . $originroom .'\'+OR+roomno+%3D+\'' . $destinationroom . '\'&returnZ=true&f=json","doNotLocateOnRestrictedElements":true}',
                    'token' => $tokenFull['token'],
                    'f' => 'json',
                    'returnDirections' => false,
                    'returnZ' => true,
                    'orderByFields' => 'roomno',
                    'barriers' => $barriers
                ))));
        $resp = curl_exec($ch);
        curl_close($ch);
        //print_r($resp);//---------------------------------------------------------------------------------------
        if(strcmp($originroom, $destinationroom) < 0)
        {
            $reverse = true;
        }
        return RouteFunctions::compilePath($resp,$reverse);
    }

    /* solveFacility navigates to closest facility from a coordinate
     * Parameters:  $x, the x coordinate
     *              $y, the y coordinate
     *              $floornum, the z coordinate (or floor number)
     *              $facilityType, the type of facility (restroom, storm shelter, etc)
     * Return:      solveRoute's return
     */
    static function solveFacility($x, $y, $floornum, $facilityType)
    {
        $facility_room = RouteFunctions::findNearest($x,$y,"floornum=" . $floornum . " AND spacetype='" . $facilityType . "'");
        $user_room = RouteFunctions::findNearest($x,$y,"floornum=" . $floornum);
        return RouteFunctions::solveRoute($user_room,$facility_room);
    }
}

/*
RouteFunctions::generateToken();
//solveRoute(findNearest($x,$y,"floornum=" . $floornum),$destinationroom); //route from arbitrary coord to destination room
//print_r(RouteFunctions::solveFacility(763735.766608133912, 2147974.4081083461, 2, "Restroom"));
//print_r(RouteFunctions::solveRoute('4.205', '4.516E'));
*/
