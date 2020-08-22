package com.example.locus_assignment

import android.util.Log
import com.example.locus_assignment.model.DataMap
import com.example.locus_assignment.model.ImageResponseItem
//import com.example.locus_assignment.view.TAG
import org.json.JSONArray
import java.util.ArrayList

class Utility {

    fun getJsonObject(obj: JSONArray): ArrayList<ImageResponseItem>{
        val imageRes = ArrayList<ImageResponseItem>()
        for (i in 0..obj.length()-1){
            val json  =  obj.getJSONObject(i)
            val type = json.getString("type")
            val id = json.getString("id")
            val title = json.getString("title")
            val dataMap = json.getJSONObject("dataMap")
            val dataList = java.util.ArrayList<String>()
            if (dataMap.length() > 0){
                val dataM = dataMap.getJSONArray("options")
                for (index in 0..dataM.length()-1){
                    dataList.add(dataM[index].toString())
                }
            }
            imageRes.add(ImageResponseItem(type = type, id = id, title = title, dataMap = DataMap(dataList)))
        }
        return imageRes
    }
}
/*

{
    "type": "SINGLE_CHOICE",
    "id": "choice1",
    "title": "Photo 1 choice",
    "dataMap": {
    "options": [
    "Good",
    "OK",
    "Bad"
    ]
}
},
{
    "type": "COMMENT",
    "id": "comment1",
    "title": "Photo 1 comments",
    "dataMap": {}
},
{
    "type": "PHOTO",
    "id": "pic2",
    "title": "Photo 2",
    "dataMap": {}
},
{
    "type": "SINGLE_CHOICE",
    "id": "choice2",
    "title": "Photo 2 choice",
    "dataMap": {
    "options": [
    "Good",
    "OK",
    "Bad"
    ]
}
},
{
    "type": "COMMENT",
    "id": "comment2",
    "title": "Photo 2 comments",
    "dataMap": {}
},
{
    "type": "COMMENT",
    "id": "comment3",
    "title": "Photo 3 comments",
    "dataMap": {}
},
{
    "type": "PHOTO",
    "id": "pic3",
    "title": "Photo 3",
    "dataMap": {}
},
{
    "type": "SINGLE_CHOICE",
    "id": "choice2",
    "title": "Photo 3 choice",
    "dataMap": {
    "options": [
    "Nice",
    "Not Nice"
    ]
}
},
{
    "type": "SINGLE_CHOICE",
    "id": "choice4",
    "title": "Photo 4 type",
    "dataMap": {
    "options": [
    "Document",
    "Face"
    ]
}
},
{
    "type": "PHOTO",
    "id": "pic4",
    "title": "Photo 4",
    "dataMap": {}
}*/
