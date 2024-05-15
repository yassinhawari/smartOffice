package com.example.smartoffice.worker


import android.annotation.SuppressLint
import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.smartoffice.AppBottomBar
import com.example.smartoffice.R
import com.example.smartoffice.group.Worker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WorkerListScreen(
    auth: FirebaseAuth,
    database:DatabaseReference,
    navController : NavHostController,
) {
    val context = LocalContext.current
    val viewModel: WorkerListViewModel = remember { WorkerListViewModel(context,database) }
    WorkerList(
        navController,
        viewModel,
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WorkerList(
    navController : NavHostController,
    viewModel: WorkerListViewModel,
) {
    val lato= FontFamily(Font(R.font.lato_bold))
    val workers by viewModel.workers.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                ),
                title = {
                    Text(
                        "Worker list",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = lato,
                    )
                },
               )},
        bottomBar = {
        AppBottomBar(navController) })
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)){
            Image(
                painter = painterResource(id = R.drawable.background_gradient_lights),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.2f))
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(workers) { worker ->
                        WorkerItem(worker = worker, viewModel)
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerItem(worker: Worker, viewModel: WorkerListViewModel) {
    var (showChart,setshowChart) = remember { mutableStateOf(false) }

    val (showDate, setshowDate) = remember { mutableStateOf(false) }

    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { setshowDate(!showDate) }
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(worker.name, style = MaterialTheme.typography.titleMedium)
            if(showDate){
                Row {
                    Text(
                        worker.timeIn,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Icon(Icons.Default.ArrowRightAlt, contentDescription = null)
                    Text(worker.timeOut, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(){
            IconButton(onClick = { setshowChart(!showChart) }) {
                Icon(Icons.Rounded.BarChart,contentDescription = null)
            }
        }
    }
    }
    if(showChart){
        workerChart(worker.name,viewModel){setshowChart(false)}}
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
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
    val data = co.yml.charts.ui.linechart.model.LineChartData(
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
        yAxisData = yAxisData
    )
    co.yml.charts.ui.linechart.LineChart(
        modifier = Modifier
            .height(300.dp),
        lineChartData = data
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun workerChart(
    workerName:String,
    viewModel: WorkerListViewModel,
    onDismiss:()->Unit,
){
    val workerData=viewModel.dataPointMap.collectAsState()
    val selectedWorkerData =   workerData.value[workerName]

    val chartData = remember(selectedWorkerData) {
        selectedWorkerData?.mapIndexed { index, dataPoint ->
            Point(
                x=dataPoint.date.substring(0, 2).toFloat(),
                y=dataPoint.totalHours
            )
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Worker chart") },
        text = {
            if (chartData != null) {
                DottedLinechart(chartData)
            }
        },
        confirmButton = {},
        dismissButton = {},
        icon={
            IconButton(
                onClick = { onDismiss() }
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
            }
        }
    )
}







