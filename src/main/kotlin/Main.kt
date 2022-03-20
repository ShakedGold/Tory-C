import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.RunProcessFunction
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.progress.Progress
import net.bramp.ffmpeg.progress.ProgressListener
import java.awt.FileDialog
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


val func = RunProcessFunction()
var percentage = 0f

@Composable
fun App() {
    func.setWorkingDirectory(System.getProperty("user.dir"))
    MaterialTheme {
        screen()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
                    val os = OSValidator.checkOS()
                    when (os) {
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
                    files = openFileDialog(
                        ComposeWindow(),
                        "Select File",
                        listOf(
                            "mp4",
                            "mov",
                            "avi",
                            "flv",
                            "gif",
                            "png",
                            "jpeg",
                            "jpg",
                            "webp",
                            "bmp",
                            "pdf",
                            "docx",
                            "txt"
                        )
                    )
                    mode = Mode.CONVERT
                }) { Text("Import Files") }
            }
        }
        Mode.CONVERT -> {
            println(percentage)
            var expanded by remember { mutableStateOf(false) }
            var alpha by remember { mutableStateOf(0f) }
            Column(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1f)) {
                    item {
                        when (determineFiletype(files)) {
                            Filetype.VIDEO -> {
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                                ) { Text("Choose Format") }
                                LinearProgressIndicator(progress = animatedProgress, modifier = Modifier.fillMaxWidth().size(10.dp).alpha(alpha))
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
                                            convertTo("flv", files, ffmpeg, ffprobe)
                                        }) { Text("FLV") }
                                        DropdownMenuItem(onClick = {
                                            thread(start = true, isDaemon = false) {
                                                while (progress < 1) {
                                                    progress = percentage
                                                }
                                            }
                                            alpha = 100f
                                            expanded = false
                                            convertTo("gif", files, ffmpeg, ffprobe)
                                        }) { Text("GIF") }
                                    }
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
                            else -> {
                                Text(
                                    "An Error has Occurred, Please Try Again",
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    fontSize = 20.sp,
                                    color = Color.Red
                                )
                            }
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

fun convertTo(s: String, files: List<File>, ffmpeg: FFmpeg, ffprobe: FFprobe) {
    files.forEach { file ->
            val executor = FFmpegExecutor(ffmpeg, ffprobe)
            val `in` = ffprobe.probe(file.path)

            val builder = FFmpegBuilder()
                .setInput(`in`)
                .addOutput("${file.name.split(".")[0]}.$s")
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .done()
            val job = executor.createJob(builder, object : ProgressListener {
                val duration_ns = `in`.getFormat().duration * TimeUnit.SECONDS.toNanos(1)
                override fun progress(progress: Progress) {
                    percentage = (progress.out_time_ns / duration_ns).toFloat()
                }
            })
            job.run()
    }
}

fun determineFiletype(files: List<File>): Filetype {
    if (files.isEmpty()) return Filetype.ERROR
    val previous = files[0].name.split(".")[1]
    val listOfVideo = listOf("mp4", "mov", "avi", "flv", "gif") // Working maybe more formats
    val listOfImage = listOf("png", "jpeg", "jpg", "webp", "bmp") // More on the way *Not working yet*
    val listOfDocument = listOf("pdf", "docx", "txt") // More on the way *Not Working Yet*
    val result = arrayListOf<File>()
    files.forEach { file ->
        if (listOfVideo.indexOf(previous) != -1 && listOfVideo.indexOf(file.name.split(".")[1]) != -1 ||
            listOfImage.indexOf(previous) != -1 && listOfImage.indexOf(file.name.split(".")[1]) != -1 ||
            listOfDocument.indexOf(previous) != -1 && listOfDocument.indexOf(file.name.split(".")[1]) != -1
        ) {
            result.add(file)
        }
    }
    if (result.size != files.size) return Filetype.ERROR
    return when (files[0].name.split(".")[1]) {
        "mp4", "mov", "avi", "flv", "gif" -> {
            Filetype.VIDEO
        }
        "png", "jpeg", "jpg", "webp", "bmp" -> {
            Filetype.IMAGE
        }
        "pdf", "docx", "txt" -> {
            Filetype.DOCUMENT
        }
        else -> Filetype.ERROR
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
