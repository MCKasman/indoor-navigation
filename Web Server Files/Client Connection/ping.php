<?php

include "./index.php";

include "./cmx_util.php";

class Ping {
  private $input;
  private $ip;
  private $cmx;
  private $path;

  // ping web-server to update current location of user
  public function pingRequest() {
    function update(){

    // get desired destination from user
    function getDestination(){
    $destination = new Request();
    $input = $destination->points();
    return $input_2;
  }

    // get user IP from index.php
    function getIP(){
    $user = new Request();
    $ip = $user->userIP();
    return $ip;
  }

    // returns updated CMX information based on the IP location of the userIP
    function getCMX(){
    $cmx = new CMXRequest("config.json", $ip);
    return $cmx;
  }


}

    // send location of user back to the client
    header("Content-Type: appplication/json");
      exit(json_encode($cmx);
  }
}
