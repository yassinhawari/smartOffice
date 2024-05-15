package com.example.smartofficeworker.stat


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.smartofficeworker.AppTopBar
import com.example.smartofficeworker.chat.Worker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateScreen(
    auth:FirebaseAuth,
    database: DatabaseReference,
    navController : NavHostController
) {
    val context = LocalContext.current
    StateMenu(navController,context ,auth,database,)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateMenu(navController : NavHostController,context: Context,auth:FirebaseAuth,database: DatabaseReference, viewModel: StateViewModel = remember { StateViewModel(auth,context,database)})
{
    Scaffold(topBar = {
        AppTopBar(
            navController,"he",auth
        )
    }) {
        Box(modifier = Modifier
            .padding(it)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)){
            val workerData=viewModel.dataPointFlow.collectAsState()
            val chartData: List<Point> = workerData.value.map { dataPoint ->
                Point(
                    x=dataPoint.date,
                    y=dataPoint.totalHours
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 12.dp)) {
                viewModel.currentWorker.value?.let { workerInfo(worker = it) }
                Spacer(modifier=Modifier.height(16.dp))
                Text(text = "Current Mounth:${viewModel.currentMounth.value}", textAlign = TextAlign.Center)
                if(!chartData.isEmpty()){
                    DottedLinechart(pointsData = chartData)
                }
            }


        }
    }
}

@Composable
private fun workerInfo(worker: Worker){
    Column {
        TextField(value = worker.name, onValueChange = {}, readOnly = true, label = { Text(text = "Name")})
        Spacer(modifier = Modifier.height(10.dp))
        TextField(value = worker.email, onValueChange ={}, readOnly = true, label = { Text(text = "E-mail")} )
    }

}

@Composable
private fun DottedLinechart(pointsData: List<Point>) {
    val steps = 10
    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp)
        .steps(pointsData.size - 1)
        .labelData { i -> i.toString() }
        .labelAndAxisLinePadding(15.dp)
        .axisLineColor(Color.Red)
        .build()
    val yAxisData = AxisData.Builder()
        .steps(steps)
        .labelData { i ->
            val yMin = pointsData.minOf { it.y }
            val yMax = pointsData.maxOf { it.y }
            val yScale = (yMax - yMin) / steps
            ((i * yScale) + yMin).formatToSinglePrecision()
        }
        .axisLineColor(Color.Red)
        .labelAndAxisLinePadding(20.dp)
        .build()
    val data = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    lineStyle = LineStyle(
                        lineType = LineType.SmoothCurve(isDotted = true),
                        color = Color.Green
                    ),
                    shadowUnderLine = ShadowUnderLine(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Green,
                                Color.Transparent
                            )
                        ), alpha = 0.3f
                    ),
                    selectionHighlightPoint = SelectionHighlightPoint(
                        color = Color.Green
                    ),
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        backgroundColor = Color.Black,
                        backgroundStyle = Stroke(2f),
                        labelColor = Color.Red,
                        labelTypeface = Typeface.DEFAULT_BOLD
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(),
    )
    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartData = data
    )
}


