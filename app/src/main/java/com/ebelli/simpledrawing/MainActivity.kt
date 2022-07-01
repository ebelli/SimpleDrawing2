package com.ebelli.simpledrawing

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.ebelli.simpledrawing.ui.theme.SimpleDrawing2Theme

enum class DrawMode {
    DRAW, TOUCH, ERASE
}

enum class Motion {
    NONE, DOWN, MOVE, UP
}
class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SimpleDrawing2Theme {
                DrawingCanvas()

            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingCanvas() {

    var drawMode by remember { mutableStateOf(DrawMode.DRAW) }
    var motionEvent by remember { mutableStateOf(Motion.NONE) }
    var currentPath by remember { mutableStateOf(Path()) }

    var currentPosition by remember { mutableStateOf(Offset.Unspecified) }
    var previousPosition by remember { mutableStateOf(Offset.Unspecified) }

    val paths = remember { mutableStateListOf<Path>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        motionEvent = Motion.DOWN
                        currentPosition = Offset(it.x, it.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        motionEvent = Motion.MOVE
                        currentPosition = Offset(it.x, it.y)
                }
                    MotionEvent.ACTION_UP -> {motionEvent = Motion.UP}
                    else -> false
                }
                true
            }
    ) {
        when (motionEvent) {

            Motion.DOWN -> {
                if (drawMode != DrawMode.TOUCH) {
                    currentPath.moveTo(currentPosition.x, currentPosition.y)
                }

                previousPosition = currentPosition

            }
            Motion.MOVE -> {

                if (drawMode != DrawMode.TOUCH) {
                    currentPath.quadraticBezierTo(
                        previousPosition.x,
                        previousPosition.y,
                        (previousPosition.x + currentPosition.x) / 2,
                        (previousPosition.y + currentPosition.y) / 2

                    )
                }

                previousPosition = currentPosition
            }
            Motion.UP -> {
                if (drawMode != DrawMode.TOUCH) {
                    currentPath.lineTo(currentPosition.x, currentPosition.y)

                    // Pointer is up save current path
                    paths.add(currentPath)

                    // Since paths are keys for map, use new one for each key
                    // and have separate path for each down-move-up gesture cycle
                    currentPath = Path()

                }
//                currentPosition = Offset.Unspecified
                previousPosition = currentPosition
                motionEvent = Motion.NONE

                with(drawContext.canvas.nativeCanvas) {

                    val checkPoint = saveLayer(null, null)

                    paths.forEach {

                        val path = it

                        drawPath(
                            color = Color.Black,
                            path = path,
                            style = Stroke(
                                width = 10f
                            )
                        )

                    }
                    restoreToCount(checkPoint)
                }
            }
            Motion.NONE -> {

            }
        }
    }
}
