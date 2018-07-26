<?php

function findNearest($x, $y, $filter = "1=1") {
    global $tokenFull;

    //Setup cURL
    $ch = curl_init();
    curl_setopt_array($ch, array(
        CURLOPT_RETURNTRANSFER => TRUE,
        CURLOPT_URL => "https://cybernetics.utdallas.edu/server/rest/services/System/SpatialAnalysisTools/GPServer/FindNearest/submitJob?" .
            http_build_query (array(
                "analysisLayer" => '{"layerDefinition":{"geometryType": "esriGeometryPoint","fields": []},"featureSet": {"geometryType": "esriGeometryPoint","spatialReference": {"wkid": 32138 },"features": [{"geometry": {"x": ' . $x . ',"y": ' . $y . '}}]}}',
                "nearLayer" => '{"url":"https://cybernetics.utdallas.edu/server/rest/services/IndoorNav/GreenFinalNet/MapServer/0","filter":' . $filter . '}',
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
        }
    } while (!$jobStatus == "esriJobSucceeded");
    curl_close($ch);

    return "testy boi";

}

print_r(findNearest());

