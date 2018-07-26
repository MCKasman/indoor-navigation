<?php

class Request{
  include ("cmx_util.php");
  include ("RouteFunctions.php");
  private $cmx;
  private $path;
  private $ip;
  private $input; // Destination of the user.

  // Get destination from the client.
  public function destination(){
    foreach ($_POST as $request => $input) {
    switch ($request) {
        case "destination":
            $destination = $input;
            break;
        default:
            break;
    }
}

  }

  // Get user IP.
  public function userIP(){
    $ip = '';

   if ($_SERVER['HTTP_CLIENT_IP']){
       $ip = $_SERVER['HTTP_CLIENT_IP'];
   }

   else if($_SERVER['HTTP_X_FORWARDED_FOR']){
       $ip = $_SERVER['HTTP_X_FORWARDED_FOR'];
   }

   else if($_SERVER['HTTP_X_FORWARDED']){
       $ip = $_SERVER['HTTP_X_FORWARDED'];
   }

   else if($_SERVER['HTTP_FORWARDED_FOR']){
       $ip = $_SERVER['HTTP_FORWARDED_FOR'];
   }

   else if($_SERVER['HTTP_FORWARDED']){
       $ip = $_SERVER['HTTP_FORWARDED'];
   }

   else if($_SERVER['REMOTE_ADDR']){
       $ip = $_SERVER['REMOTE_ADDR'];
   }
   else{
       $ip = 'UNKNOWN';
   }

   return $ip;
}

  // Recieve JSON data from CMX server.
  public function cmxRequest(){
    $cmx = new CMXRequest("config.json", $ip){
      $cmx->getResponse();
    }
  // Receive path from ARCGIS.
  public function routeRequest(){
    $path = new RouteFunctions(){
      $json = $path->getPath($cmx, $input);
      $route = $path->compilePath($json);
    }

  }
  // Return path from ARCGIS to the client.
    header("Content-Type: application/json");
    echo json_encode($route);


}
