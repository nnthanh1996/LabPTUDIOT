<?php

use Slim\Http\Request;
use Slim\Http\Response;

// Routes into home

// $app->get('/', function (Request $request, Response $response, array $args) {
//     // Sample log message
//     $this->logger->info("Slim-Skeleton '/' route");

//     // Render index view
//     return $this->renderer->render($response, 'index.phtml', $args);
// });

$app->get('/',App\DataController::class.':getAllData');

$app->get('/gallery',App\DataController::class.':getGallery');

// Push tempature to database
$app->post('/rest/iotdata/',App\DataController::class.':pushData');

// Push tempature to database
$app->get('/rest/iotdata/gallery/{id}',App\DataController::class.':getAPIGallery');

// Get All Data via API

$app->get('/rest/iotdata/{id}',App\DataController::class.':APIgetData');

// Push humiddity to database
$app->get('/rest/temperature/{id}', App\DataController::class.':APIgetTemperature');

// Push humiddity to database
$app->get('/rest/humidity/{id}', App\DataController::class.':APIgetHumidity');

// Push pressure to database
$app->get('/rest/light/{id}', App\DataController::class.':APIgetLight');

$app->get('/rest/gas/{id}', App\DataController::class.':APIgetGas');






