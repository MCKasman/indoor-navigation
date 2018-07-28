<?php

include "./cmx_util.php";
include "./RouteFunctions.php";

class Request {
  private $cmx;
  private $path;
  private $input;

  // destination of the user
  private $ip;

  // get destination from the client
  public function destination() {
    $input = $_POST["destination"];
  }

  // get user IP
  public function userIP() {
      $ip = '';

     // identify the IP address of the host that is making the HTTP request
     if ($_SERVER['HTTP_CLIENT_IP'])  {
         $ip = $_SERVER['HTTP_CLIENT_IP'];
     }
     else if($_SERVER['HTTP_X_FORWARDED_FOR'])  {
         $ip = $_SERVER['HTTP_X_FORWARDED_FOR'];
     }
     else if($_SERVER['HTTP_X_FORWARDED'])  {
         $ip = $_SERVER['HTTP_X_FORWARDED'];
     }
     else if($_SERVER['HTTP_FORWARDED_FOR'])  {
         $ip = $_SERVER['HTTP_FORWARDED_FOR'];
     }
     else if($_SERVER['HTTP_FORWARDED'])  {
         $ip = $_SERVER['HTTP_FORWARDED'];
     }
     // refer to the IP address of the client
     else if($_SERVER['REMOTE_ADDR'])  {
         $ip = $_SERVER['REMOTE_ADDR'];
     }
     else {
         $ip = 'UNKNOWN';
     }

     return $ip;
  }

  public function servers() {

    // recieve JSON data from CMX server
    $cmx = new CMXRequest("config.json", $ip);
    return $cmx;

    // receive path from ARCGIS
    function routeRequest()  {
      $path = new RouteFunctions();
      $json = $path->getPath($cmx, $input);
      $route = $path->compilePath($json);
      return $route;
    }

    // return path from ARCGIS to the client
    header("Content-Type: application/json");
      exit(json_encode($route));
  }
}
