<?php

include "./index.php";
include "./cmx_util.php";

class Ping {
  private $cmx;
  private $ip;

  // ping web-server to update current location of user
  public function pingRequest() {
    function update(){

    // get user IP from index.php
    $user = new index();
    $ip = $user->userIP();
    return $ip;

    // returns updated cmx information based on the IP location of the userIP
    $cmx = new CMXRequest("config.json", $ip);
    return $cmx;
    }

    // send location of user back to the client
    header("Content-Type: appplication/json");
      exit(json_encode($cmx));
  }
}
