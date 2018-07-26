<?php
  # $_SERVER SUPERGLOBAL

  // create Server Array
  $server  = [
    "Host Server Name" => $_SERVER["Constellation"],
    "Host Header" => $_SERVER["HTTP_HOST"],
    "Server Software" => $_SERVER["SERVER SOFTWARE"],
    "Document Root" => $_SERVER["DOCUMENT_ROOT"]
  ];

  // create Client Arrayx
  $client = [
    "Client System Info" => $_SERVER["HTTP_USER_AGENT"],
    "Client IP" => $_SERVER["REMOTE_ADDR"],
    "Remote Port" => $_SERVER["REMOTE_PORT"],
  ];
