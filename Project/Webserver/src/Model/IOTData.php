<?php 

namespace App\Model;

use Illuminate\Database\Eloquent\Model;

class IOTData extends Model {
   protected $table = 'datacollecting';
   protected $fillable = ['Temperature','Humidity','Light','Gas'];
   public $timestamps = false;
}