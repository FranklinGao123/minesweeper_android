package com.example.portfolio_app

import android.graphics.Paint.Align
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portfolio_app.ui.theme.Portfolio_AppTheme
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.util.LinkedList
import java.util.Queue

val ROWS = 20
var COLUMNS = 20
val TOTAL = ROWS * COLUMNS
val DEBUG = false
var first = true
var total = 0
var left = TOTAL
var endGame by mutableStateOf(false)
enum class Difficulty(val num: Double){
    EASY(0.1), MEDIUM(0.15), HARD(0.20), IMPOSSIBLE(0.80)
}
enum class Action(val color: Color){
    NONE(Color.LightGray), PRESSED(Color.White), DEBUG(Color.Red)
}
class Box(){
    var mine = false
    var action by mutableStateOf(Action.NONE)
    var flagged by mutableStateOf(false)
    var adj = 0

    constructor(mine: Boolean, action: Action) : this() {
        this.mine = mine
        this.action = action
    }

}
var map = Array(ROWS){ Array(COLUMNS){Box()} }
var dir = arrayOf(arrayOf(-1,-1),arrayOf(0,-1),arrayOf(1,-1),
    arrayOf(-1,0),arrayOf(0,0),arrayOf(1,0),
    arrayOf(-1,1),arrayOf(0,1),arrayOf(1,1))


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Portfolio_AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    boardPreview()
                }
            }
        }
    }
}

@Composable
fun size(){
    val configuration = LocalConfiguration.current
    COLUMNS = configuration.screenWidthDp/20
}


fun initBoard(){
    first = true
    total = 0
    var mines = BooleanArray(TOTAL)
    var difficulty = Difficulty.MEDIUM
    for (i in 0 .. (TOTAL * difficulty.num).toInt()){
        mines[i] = true
        total++
    }
    mines.shuffle()
    for (i in 0..ROWS-1){
        for (j in 0..COLUMNS-1){
            map[i][j].mine = mines[i * ROWS + j]
            if(DEBUG && mines[i * ROWS + j]){
                map[i][j].action = Action.DEBUG
            }else{
                map[i][j].action = Action.NONE
            }
            map[i][j].flagged = false
        }
    }
    for (i in 0..ROWS-1){
        for (j in 0..COLUMNS-1){
            var count = 0
            for((x,y) in dir){
                if(i + x >= 0 && i + x < ROWS){
                    if(j + y >= 0 && j + y < COLUMNS){
                        if(map[i+x][j+y].mine){
                            count++
                        }
                    }
                }
            }
            map[i][j].adj = count
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun setupBoard(modifier: Modifier = Modifier) {
    Column (modifier = Modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center){
        for (i in 0..ROWS-1){
            Row (modifier = Modifier
                .align(Alignment.CenterHorizontally)){
                for (j in 0..COLUMNS - 1) {
                    board(i,j)
                }
            }
        }
    }
    if(endGame){
        endScreen()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun board(i: Int, j:Int){
    Box(modifier = Modifier
        .background(map[i][j].action.color)
        .size(20.dp)
        .border(1.dp, Color.DarkGray)
        .combinedClickable(
            onClick = {
                if (map[i][j].flagged == false) {
                    if (first && map[i][j].mine) {
                        map[i][j].mine = false
                        for ((a, b) in dir) {
                            if (a + i >= 0 && a + i < ROWS) {
                                if (b + j >= 0 && b + j < COLUMNS) {
                                    map[i+a][j+b].adj--
                                }
                            }
                        }
                        left--
                    }
                    if (map[i][j].mine) {
                        //Game Over
                        for (i in 0..ROWS - 1) {
                            for (j in 0..COLUMNS - 1) {
                                if (map[i][j].mine) {
                                    map[i][j].action = Action.DEBUG
                                } else if (map[i][j].action == Action.NONE) {
                                    map[i][j].action = Action.PRESSED
                                }
                            }
                        }
                        endGame = true
                    } else if (map[i][j].action == Action.NONE) {
                        var q: Queue<Pair<Int, Int>> = LinkedList<Pair<Int, Int>>()
                        q.add(Pair(i, j))
                        while (!q.isEmpty()) {
                            var (x, y) = q.poll()
                            for ((a, b) in dir) {
                                if (a + x >= 0 && a + x < ROWS) {
                                    if (b + y >= 0 && b + y < COLUMNS) {
                                        if (!map[a + x][b + y].mine && map[a + x][b + y].action == Action.NONE) {
                                            map[a + x][b + y].action = Action.PRESSED
                                            left--
                                            if (map[a + x][b + y].adj == 0) {
                                                q.add(Pair(a + x, b + y))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(left == 0){
                            endGame = true
                        }
                    }
                    first = false

//                    map[i][j].action = Action.PRESSED
//                    if(map[i][j].adj == 0){
//                        for((x,y) in dir){
//                            if(i + x >= 0 && i + x < ROWS){
//                                if(j + y >= 0 && j + y < COLUMNS){
//                                    if(!map[i+x][j+y].mine){
//                                        map[i+x][j+y].action = Action.PRESSED
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
            },
            onLongClick = {
                map[i][j].flagged = !map[i][j].flagged
                if (map[i][j].mine) {
                    total--
                    if (total == 0) {
                        endGame = true
                    }
                }
            }
        ),
    ){
        if(map[i][j].flagged){
            Image(
                painter = painterResource(id = R.drawable.flag),
                contentDescription = "Flag"
            )
        }
        if(map[i][j].action == Action.PRESSED && map[i][j].adj != 0){
            Text(map[i][j].adj.toString(),
                modifier = Modifier
                    .border(0.dp, Color.Transparent)
                    .fillMaxSize()
                    .padding(1.dp),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )

        }
    }
}

@Composable
fun endScreen(){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(modifier = Modifier
            .width(300.dp)
            .height(150.dp)
            .background(Color.White)
            .border(2.dp, Color.Black),
            contentAlignment = Alignment.Center
        ){
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    modifier = Modifier
                        .width(300.dp),
                    text = if(total == 0) "Congratulations!" else "Try Again!",
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = {
                        endGame = false
                        initBoard()
                }){
                    Text(text = "Restart")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun boardPreview() {
    Portfolio_AppTheme {
        size()
        initBoard()
        setupBoard()
    }
}