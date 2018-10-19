<?php

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

include_once "cmx_util/cmx_util.php";
include_once "arcGIS/RouteFunctions.php";

class Request {
  protected $originroom;
  protected $destinationroom;
  protected $ip;
  protected $cmx = "";
  protected $path = "";
  protected $stairs = "";
  protected $elevators = "";

  // get start and end destination from the client
  public function points() {

    $this->originroom = $_POST["from"];
    $this->destinationroom = $_POST["to"];
    $this->stairs = $_POST["stairs"];
    $this->elevators = $_POST["elevators"];

    //$this->originroom = "4.205";
    //$this->destinationroom = "4.516E";
    if ($_SERVER['REQUEST_METHOD']=='POST') {
      echo("Hello World!\n $this->originroom \n $this->destinationroom \n $this->stairs \n $this->elevators");
      print_r($_POST);
    }
  }

  // get user IP
  public function userIP() {

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

     echo $ip . "\n \n";
     return $ip;

  }

  public function destinationRoomIsSet()
  {
	  return (empty($this->destinationroom) == FALSE);
  }

  public function originRoomIsSet()
  {
	  return (empty($this->originroom) == FALSE);
  }

  public function getRoute() {

    // receive path from ARCGIS
    $path = new RouteFunctions();

    if($this->originRoomIsSet() && $this->destinationRoomIsSet()) {
        $json = $path->getPath($this->originroom, $this->destinationroom, $this->stairs, $this->elevators);
        //$route = $path->compilePath($json);
    }

    else if($this->destinationRoomIsSet()){
        // recieve JSON data from CMX server
        $cmx = new CMXRequest("cmx/config.json", $this->userIP());
        echo $this->userIP() . "\n \n";
        print_r ($cmx->getResponse());
        // return $cmx;

        $json = $path->getPath($cmx->getResponse(), $this->destinationroom, $this->stairs, $this->elevators);
        //$route = $path->compilePath($json);
    }

    else if($this->originRoomIsSet()){
        $json = RouteFunctions::queryRoomNum($this->originroom);
    }
    else {
      $cmx = new CMXRequest("cmx/config.json", $this->userIP());
      $json = $path->queryRoomNum($path->parseCMXPoint($cmx->getResponse()));
    }

    return $json;

  }

  public function handle()
  {
    RouteFunctions::generateToken();
    Request::points();
    //Request::userIP();


    // return path from ARCGIS to the client
    header("Content-Type: application/json");
    // echo RouteFunctions::solveRoute($originroom, $destinationroom);
    echo $this->getRoute();
  }
}

(new Request())->handle();
