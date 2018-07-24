<?php

$tokenFull = array();

class RouteFunctions
{

    /* compilePath creates a usable coordinate array for the User Interface group to use.
     * Parameters:  $json, a raw json object directly from ArcGIS containing the coordinate locations for the path
     * Return:      $route, a 1 dimensional array containing points in the form (z1, x1, y1, z2, x2, y2,... zn, xn, yn)
     */
    public function compilePath($json)
    {

        //In case $json is empty
        if (empty($json)) {
            return "[]";
        }

        //Extract arrays from raw json
        $arcGISData = json_decode($json, TRUE);

        //Initiate variables
        $route = array();
        $xCoordList = array();
        $yCoordList = array();
        $zCoordList = array();
        $counter = 0;


        //Construct coord lists from $arcGISData
        foreach ($arcGISData['routes']['features'][0]['geometry']['paths'][0] as $item) {
            $xCoordList[$counter] = $item[0];
            $yCoordList[$counter] = $item[1];
            $zCoordList[$counter] = $item[2];
            //echo $item[2].", ".$item[0].", ".$item[1]."<br>";
            $counter++;
        }

        //Construct $route from $zCoordList, $xCoordList and $yCoordList
        //$route will be a 1d array containing [z1, x1, y1, z2, x2, y2,... zn, xn, yn]
        for ($counter = 0; $counter < count($xCoordList); $counter++) {
            $route[$counter * 3] = $zCoordList[$counter];
            $route[$counter * 3 + 1] = $xCoordList[$counter];
            $route[$counter * 3 + 2] = $yCoordList[$counter];
        }

        //return compiled json encoded route
        return json_encode($route);
    }

    /* getPath sends a request to the ArcGIS server to receive a raw json object for the path.
     * Parameters:  $CMX, the starting location in coordinate form (x, y, z), given through CMX. Should be an
     *                  associative array, although checks are made to test and decode possible json.
     *              $end, the ending destination in coordinate form (x, y, z)
     * Return:      $json, a raw json object containing arcGIS' output
     */
    public function getPath($CMX, $end)
    {
        $startCoord = array();
        $startTemp = array();
        $startRoom = '';
        $endCoord = array();
        $endTemp = array();
        $endRoom = '';

        //Test $CMX to see if it is an associative array, a raw json object, or a room number.
        if (isJson($CMX) == TRUE) {
            $this->$startTemp = json_decode($CMX, TRUE);
        }
        elseif (is_array($CMX)) {
            $this->$startTemp = $CMX;
        } //If none/invalid
        else {
            return "Error: Invalid input. Raw json, associative array, or room number required.";
        }

        //Test $end to see if it is an associative array, a raw json object, or a room number.
        if (isJson($end) == TRUE) {
            $this->$endTemp = json_decode($end, TRUE);
        }
        elseif (is_array($end)) {
            $this->$endTemp = $end;
        } //If none/invalid
        else {
            return "Error: Invalid input. Raw json, associative array, or room number required.";
        }

        //If $CMX is a room number
        if (is_string($CMX)){
            $this->$startRoom = $CMX;
        }
        //If $end is a room number
        if (is_string($end)){
            $this->$endRoom = $end ;
        }

        //Creates $startCoord, a coordinate in the (z, x, y) form if $CMX is not a room number
        if ($startRoom == '')
        {
            $startCoord[0] = $startTemp["response"]["mapCoordinate"][1];
            $startCoord[1] = $startTemp["response"]["mapCoordinate"][2];
            $startCoord[2] = getFloor($startTemp["response"]["mapInfo"]["mapHierarchy"]);
        }

        //Creates $endCoord, a coordinate in the (z, x, y) form if $end is not a room number
        if ($endRoom == '')
        {
            $endCoord[0] = $endTemp["response"]["mapCoordinate"][1];
            $endCoord[1] = $endTemp["response"]["mapCoordinate"][2];
            $endCoord[2] = getFloor($endTemp["response"]["mapInfo"]["mapHierarchy"]);
        }


        return '';/*POST to ArcGIS containing $start and $end*/

    }


    /* generateToken creates a token request with the information needed to request a route.
     * Parameters:  $username, the username for creating a post request
     *              $password, the password for post requests
     *              $client, default page to work with (arcGIS route solver)
     *              $f, the format of the requests (pjson)
     * Returns:     $token, the generated token
     */
    function generateToken()
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
}

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
RouteFunctions::generateToken();
print_r($tokenFull);
echo "<br><br>" . $tokenFull['token'];
