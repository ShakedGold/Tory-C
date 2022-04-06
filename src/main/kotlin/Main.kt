import Enums.Filetype
import Enums.Mode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.progress.Progress
import net.bramp.ffmpeg.progress.ProgressListener
import java.awt.FileDialog
import java.io.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


var percentage = 0f
var counterFiles = 0
var options = listOf(15L, 60, "1080")

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {
    MaterialTheme {
        screen()
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun screen() {
    var mode by remember { mutableStateOf(Mode.IMPORT) }
    var files by remember { mutableStateOf(listOf<File>()) }
    var ffmpeg by remember { mutableStateOf(FFmpeg()) }
    var ffprobe by remember { mutableStateOf(FFprobe()) }
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress = animateFloatAsState(targetValue = progress, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec).value
    when (mode) {
        Mode.IMPORT -> {
            counterFiles = 0
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row {
                    Text("Click On ", fontSize = 20.sp)
                    Text("Import Files", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(" to Start Converting!", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.padding(20.dp))
                Button(onClick = {
                    when (OSValidator.checkOS()) {
                        "Windows" -> {
                            ffmpeg =
                                FFmpeg("${System.getProperty("user.dir")}\\src\\main\\kotlin\\FFmpeg Files\\Windows\\ffmpeg")
                            ffprobe =
                                FFprobe("${System.getProperty("user.dir")}\\src\\main\\kotlin\\FFmpeg Files\\Windows\\ffprobe")
                        }
                        "Mac" -> {
                            ffmpeg = FFmpeg("${System.getProperty("user.dir")}/src/main/kotlin/FFmpeg Files/Mac/ffmpeg")
                            ffprobe =
                                FFprobe("${System.getProperty("user.dir")}/src/main/kotlin/FFmpeg Files/Mac/ffprobe")
                        }
                        "Linux" -> {
                            ffmpeg =
                                FFmpeg("${System.getProperty("user.dir")}/src/main/kotlin/FFmpeg Files/Linux/ffmpeg")
                            ffprobe =
                                FFprobe("${System.getProperty("user.dir")}/src/main/kotlin/FFmpeg Files/Linux/ffprobe")
                        }
                        else -> {
                            ffmpeg =
                                FFmpeg("${System.getProperty("user.dir")}\\src\\main\\kotlin\\FFmpeg Files\\Windows\\ffmpeg")
                            ffprobe =
                                FFprobe("${System.getProperty("user.dir")}\\src\\main\\kotlin\\FFmpeg Files\\Windows\\ffprobe")
                        }
                    }
                    files = openFileDialog(ComposeWindow(), "Choose Files", listOf("mov", "mp4", "flv", "avi", "png", "jpeg"), true)
                    mode = Mode.CONVERT
                }) { Text("Import Files") }
                Spacer(modifier = Modifier.padding(10.dp))
                Text(modifier = Modifier.align(Alignment.CenterHorizontally), text = "Please don't import different types of formats for example: mp4 and a txt, in the same conversion", color = Color.Gray, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
        }
        Mode.CONVERT -> {
            var expanded by remember { mutableStateOf(false) }
            var alpha by remember { mutableStateOf(0f) }
            var currentlyDoing by remember { mutableStateOf("Converting...") }
            Column(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1f)) {
                    item {
                        when (determineFilesType(files)) {
                            Filetype.VIDEO -> {
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                                ) { Text("Choose Format") }
                                if (alpha == 100f) {
                                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                        Text(currentlyDoing, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        LinearProgressIndicator(progress = animatedProgress, modifier = Modifier.fillMaxWidth().size(10.dp).alpha(alpha))
                                    }
                                }
                                if (animatedProgress > 0.99) currentlyDoing = "Finished Converting $counterFiles Files"
                                Column {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        DropdownMenuItem(onClick = {
                                            thread(start = true, isDaemon = false) {
                                                while (progress <= 1) {
                                                    progress = percentage
                                                }
                                            }
                                            alpha = 100f
                                            expanded = false
                                            convertTo("mov", files, ffmpeg, ffprobe)
                                        }) { Text("MOV") }
                                        DropdownMenuItem(onClick = {
                                            thread(start = true, isDaemon = false) {
                                                while (progress <= 1) {
                                                    progress = percentage
                                                }
                                            }
                                            alpha = 100f
                                            expanded = false
                                            convertTo("mp4", files, ffmpeg, ffprobe)
                                        }) { Text("MP4") }
                                        DropdownMenuItem(onClick = {
                                            thread(start = true, isDaemon = false) {
                                                while (progress <= 1) {
                                                    progress = percentage
                                                }
                                            }
                                            alpha = 100f
                                            expanded = false
                                            convertTo("avi", files, ffmpeg, ffprobe)
                                        }) { Text("AVI") }
                                        DropdownMenuItem(onClick = {
                                            thread(start = true, isDaemon = false) {
                                                while (progress <= 1) {
                                                    progress = percentage
                                                }
                                            }
                                            alpha = 100f
                                            expanded = false
                                            var error = convertTo("flv", files, ffmpeg, ffprobe)
                                            println(error)
                                        }) { Text("FLV") }
                                        DropdownMenuItem(onClick = {
                                            thread(start = true, isDaemon = false) {
                                                while (progress <= 1) {
                                                    progress = percentage
                                                }
                                            }
                                            alpha = 100f
                                            expanded = false
                                            convertTo("gif", files, ffmpeg, ffprobe)
                                        }) { Text("GIF") }
                                    }
                                    Expandable()
                                }
                            }
                            Filetype.IMAGE -> {
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                                ) {
                                    Text("Choose Format")
                                }
                                Column {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        DropdownMenuItem(onClick = {}) { Text("PNG") }
                                        DropdownMenuItem(onClick = {}) { Text("JPG") }
                                        DropdownMenuItem(onClick = {}) { Text("JPEG") }
                                        DropdownMenuItem(onClick = {}) { Text("WEBP") }
                                        DropdownMenuItem(onClick = {}) { Text("BMP") }
                                    }
                                }
                            }
                            Filetype.DOCUMENT -> {
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                                ) {
                                    Text("Choose Format")
                                }
                                Column {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        DropdownMenuItem(onClick = {}) { Text("PDF") }
                                        DropdownMenuItem(onClick = {}) { Text("DOCX") }
                                        DropdownMenuItem(onClick = {}) { Text("TXT") }
                                    }
                                }
                            }
                            Filetype.ERROR -> {
                                Text(
                                    "An Error has Occurred, Please Try Again",
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    fontSize = 20.sp,
                                    color = Color.Red
                                )
                            }
                            Filetype.NOT_DEFINED -> {}
                        }
                    }
                }
                Button(
                    onClick = { mode = Mode.IMPORT },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)
                ) {
                    Text("Back")
                }
            }
        }
        else -> {}
    }
}

fun convertTo(s: String, files: List<File>, ffmpeg: FFmpeg, ffprobe: FFprobe): String {
    var error = ""
    thread(start = true, isDaemon = false) {
        files.forEach { file ->
            val executor = FFmpegExecutor(ffmpeg, ffprobe)
            val `in` = ffprobe.probe(file.path)


            val builder = FFmpegBuilder()
                .setInput(`in`)
            if(s.lowercase() == file.extension.lowercase()) {
                error = "Cant convert to the same filetype"
                return@thread
            }
            else {
                if (s == "gif") {
                    builder.addOutput("${System.getProperty("user.dir")}/src/main/kotlin/Result/${file.nameWithoutExtension}.$s")
                        .setVideoBitRate(100_000 * options[0].toString().toLong())
                        .setVideoFrameRate(options[1].toString().toDouble())
                        .addExtraArgs("-vf","scale=-1:${options[2]}")
                        .setAudioCodec("copy")
                        .done()
                }
                else {
                    builder.addOutput("${System.getProperty("user.dir")}/src/main/kotlin/Result/${file.nameWithoutExtension}.$s")
                        .setVideoBitRate(100_000 * options[0].toString().toLong())
                        .setVideoFrameRate(options[1].toString().toDouble())
                        .addExtraArgs("-vf","scale=-1:${options[2]}")
                        .setVideoCodec("h264")
                        .setAudioCodec("copy")
                        .done()
                }
            }
            val job = executor.createJob(builder, object : ProgressListener {
                val duration_ns = `in`.getFormat().duration * TimeUnit.SECONDS.toNanos(1)
                override fun progress(progress: Progress) {
                    percentage = (progress.out_time_ns / duration_ns).toFloat()
                }
            })
            job.run()
            counterFiles++
        }
    }
    return error
}

fun determineFilesType(files: List<File>): Filetype {
    if (files.isEmpty()) return Filetype.ERROR
    else if (files.size == 1) return determineFileType(files[0].extension)

    var current: String = files[1].extension
    var prev: String = files[0].extension
    files.forEach { file ->
        if (determineFileType(current) != determineFileType(prev)) return Filetype.ERROR
        current = file.extension
        prev = current
    }
    return determineFileType(current)
}

fun determineFileType(fileExt: String) : Filetype {
    return when(fileExt.lowercase()) {
        "mp4", "mov", "gif", "flv" -> {
            Filetype.VIDEO
        }
        "png", "jpeg", "jpg", "webp", "bmp" -> {
            Filetype.IMAGE
        }
        "pdf", "docx", "txt" -> {
            Filetype.DOCUMENT
        }
        else -> {
            Filetype.ERROR}
    }
}

fun openFileDialog(
    window: ComposeWindow,
    title: String,
    allowedExtensions: List<String>,
    allowMultiSelection: Boolean = true
): List<File> {
    return FileDialog(window, title, FileDialog.LOAD).apply {
        isMultipleMode = allowMultiSelection

        // windows
        file = allowedExtensions.joinToString(";") { "*$it" } // e.g. '*.jpg'

        // linux
        setFilenameFilter { _, name ->
            allowedExtensions.any {
                name.endsWith(it)
            }
        }

        isVisible = true
    }.files.toList()
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "File Convertor",
        state = WindowState(size = DpSize(430.dp, 400.dp), position = WindowPosition(Alignment.Center))
    ) {
        App()
    }
}


@ExperimentalAnimationApi
@Composable
fun Expandable() {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.clickable {
            isExpanded = !isExpanded
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row {
                Text(text = "Video Options",
                    modifier = Modifier.padding(4.dp))
                Icon(
                    Icons.Default.run {
                        if (isExpanded)
                            KeyboardArrowDown
                        else
                            KeyboardArrowUp
                    },
                    contentDescription = "",
                    tint = Color.LightGray,
                )
            }
            AnimatedVisibility(visible = isExpanded, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Column {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        var bitrateText by remember { mutableStateOf(options[0].toString()) }
                        var fpsText by remember { mutableStateOf(options[1]) }
                        var quality by remember { mutableStateOf(options[2]) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Select Bitrate (Mbps)")
                            Spacer(modifier = Modifier.padding(10.dp))
                            OutlinedTextField(
                                placeholder = {Text("For Example: 20")},
                                value = bitrateText,
                                onValueChange = { value ->
                                        bitrateText = value.filter { it.isDigit() }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.padding(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Select FPS")
                            Spacer(modifier = Modifier.padding(10.dp))
                            OutlinedTextField(
                                placeholder = {Text("For Example: 60")},
                                value = fpsText.toString(),
                                onValueChange = { value ->
                                    fpsText = value.filter { it.isDigit() }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.padding(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var qualityExpanded by remember { mutableStateOf(false) }
                            Button(onClick = {qualityExpanded = true}) {Text("Select Resolution")}
                            DropdownMenu(expanded = qualityExpanded, onDismissRequest = {qualityExpanded = !qualityExpanded}) {
                                DropdownMenuItem(onClick = {
                                    qualityExpanded = false
                                    quality = "1080p"
                                }) {Text("1080")}
                                DropdownMenuItem(onClick = {
                                    qualityExpanded = false
                                    quality = "720p"
                                }) {Text("720")}
                                DropdownMenuItem(onClick = {
                                    qualityExpanded = false
                                    quality = "480"
                                }) {Text("480p")}
                                DropdownMenuItem(onClick = {
                                    qualityExpanded = false
                                    quality = "360"
                                })
                                {Text("360p")}
                            }
                            Spacer(modifier = Modifier.padding(3.dp))
                            Text("Selected Resolution: ${quality}p")
                        }
                        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Row {
                                Button(onClick = {
                                    options = listOf(bitrateText, fpsText, quality)
                                    isExpanded = false
                                    println("${options[0]}, ${options[1]}, ${options[2]}")
                                }){ Text("Apply") }
                                Spacer(modifier = Modifier.padding(20.dp))
                                Button(onClick = {
                                    isExpanded = false
                                    println("${options[0]} ${options[1]}, ${options[2]}")
                                }){ Text("Cancel") }
                            }
                        }
                    }
                }
            }
        }

    }

}