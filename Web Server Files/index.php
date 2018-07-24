<?php

class Request{
  include ("cmx_util.php");
  include ("RouteFunctions.php");
  private $cmx;
  private $path;
  private $ip;
  private $destination;

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

  // Requests JSON data from CMX server.

  public function cmxRequest(){
    $cmx = new CMXRequest("config.json"){
      $cmx->getResponse($ip);
    }
  // Requests path from ARCGIS.
  public function routeRequest(){
    $path = new RouteFunctions(){
      $json = $path->getPath($cmx, $destination);
      $route = $path->compilePath($json);
    }
  // Returns path from ARCGIS to the client.
  }


}
