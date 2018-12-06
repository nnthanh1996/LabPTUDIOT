<?php 
namespace App;
require 'Model/IOTData.php';
use Psr\Log\LoggerInterface;
use Illuminate\Database\Query\Builder;
use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Message\ResponseInterface as Response;
use Slim\Views\PhpRenderer;
use App\Model\IOTData as IOTData;

class DataController {
    private $view;
    private $logger;
    protected $table;

    public function __construct(
        PhpRenderer $view,
        LoggerInterface $logger,
        Builder $table
    ) {
        $this->view = $view;
        $this->logger = $logger;
        $this->table = $table;
    }

    public function getAllData(Request $request, Response $response, $args)
    {
        $data = $this->table->get();

        $this->view->render($response, 'index.phtml', [
            'data' => $data
        ]);

        return $response;
    }

    public function getGallery(Request $request, Response $response, $args)
    {
        $this->view->render($response, 'gallery.phtml');

        return $response;
    }

    public function getAPIGallery(Request $request, Response $response, $args) {
        $id = $args["id"];
        $value = $this->table->where('ID',$id)->value('Time');
        $value = trim($value," ");
        $value = str_replace(':', '-', $value);
        $value = str_replace(' ', '-', $value);
        $value .= '.png';
        
        foreach (glob('image\*.png') as $filename) {
            if(strpos($filename, $value) !== false) {
                $imageData = file_get_contents($filename);
                $base64ImageEncode = base64_encode($imageData);

                $result = [
                    'ID' => $id,
                    'Image' => $base64ImageEncode
                ];
                $body = $response->getBody();
                $body->write(json_encode($result));

                return $response;
            }
        }

    }

    public function convertImage($base64_string, $output_file){
       $file = fopen($output_file, "wb");

        $data = explode(',', $base64_string);

        fwrite($file, base64_decode($data[0]));
        fclose($file);

        return $output_file;
    }

    public function pushData(Request $request, Response $response, $args){
        $data = $request->getBody();
        $dataArray =json_decode($data, true);
        $pushData = IOTData::create([
            'Temperature' => $dataArray["Temperature"],
            'Humidity' => $dataArray["Humidity"],
            'Light' => $dataArray["Light"],
            'Gas' => $dataArray["Gas"]
        ]);

        $base64Image = $dataArray["Image"];

        if(isset($base64Image)){
            $this->convertImage($base64Image, 'image/'.date('Y-m-d-h-i-s').'.png');
        }

        $body = $response->getBody();
        $body->write("Data has pushed succesfully!");
        return $response;
    }

    public function APIgetData(Request $request, Response $response, $args){
        $id = $args["id"];

        $record = $this->table->find($id);
        $resultData = json_decode(json_encode($record), True);
        $body = $response->getBody();
        $body->write(json_encode($resultData));
    }

    public function APIgetTemperature(Request $request, Response $response, $args){
        $id = $args["id"];
        $value = $this->table->where('ID',$id)->value('Temperature');
        $result = [
            'ID' => $id,
            'Temperature' => $value
        ];
        $body = $response->getBody();
        $body->write(json_encode($result));

    }

    public function APIgetHumidity(Request $request, Response $response, $args){
        $id = $args["id"];
        $value = $this->table->where('ID',$id)->value('Humidity');
        $result = [
            'ID' => $id,
            'Humidity' => $value
        ];
        $body = $response->getBody();
        $body->write(json_encode($result));
    }

    public function APIgetLight(Request $request, Response $response, $args){
        $id = $args["id"];
        $value = $this->table->where('ID',$id)->value('Light');
        $result = [
            'ID' => $id,
            'Light' => $value
        ];
        $body = $response->getBody();
        $body->write(json_encode($result));
    }

    public function APIgetGas(Request $request, Response $response, $args){
        $id = $args["id"];
        $value = $this->table->where('ID',$id)->value('Gas');
        $result = [
            'ID' => $id,
            'Gas' => $value
        ];
        $body = $response->getBody();
        $body->write(json_encode($result));
    }
}   