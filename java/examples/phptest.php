<?php
/*************************************************
 * Simple php rest call tester using curl
 *
 * @author  mgill
 * @version 190612
 *************************************************                                                                                                                                        
 */
$curl = curl_init();
curl_setopt($curl, CURLOPT_POST, 1);
curl_setopt($curl, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
curl_setopt($curl, CURLOPT_USERPWD, "mgill:iamakisa");
curl_setopt($curl, CURLOPT_URL, "http://localhost:9000/api/test");
curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);    // return json as a string 
$json = curl_exec($curl);
$size = strlen($json);
header("Content-Type: application/json");
header("Content-Length: ".$size+1);
echo $json;
?>