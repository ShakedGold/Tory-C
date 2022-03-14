import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
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


val func = RunProcessFunction()

@Composable
fun App() {
    func.setWorkingDirectory(System.getProperty("user.dir"))
    MaterialTheme {
        screen()
    }
}

@Composable
fun screen() {
    var mode by remember { mutableStateOf(Mode.IMPORT) }
    var files by remember { mutableStateOf(setOf<File>()) }
    var ffmpeg by remember { mutableStateOf(FFmpeg()) }
    var ffprobe by remember { mutableStateOf(FFprobe()) }
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
                    files = openFileDialog(ComposeWindow(), "Select File", listOf(".mov"))
                    mode = Mode.CONVERT
                }) { Text("Import Files") }
            }
        }
        Mode.CONVERT -> {
            var expanded by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1f)) {
                    item {
                        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                            Text("Choose Format")
                        }
                        Column {
                            DropdownMenu(expanded = expanded, onDismissRequest = {expanded = false}, modifier = Modifier.fillMaxWidth()) {
                                DropdownMenuItem(onClick = { convertTo("MOV", files, ffmpeg, ffprobe) }) { Text("MOV") }
                                DropdownMenuItem(onClick = { convertTo("MP4", files, ffmpeg, ffprobe) }) { Text("MP4") }
                                DropdownMenuItem(onClick = { convertTo("AVI", files, ffmpeg, ffprobe) }) { Text("AVI") }
                                DropdownMenuItem(onClick = { convertTo("FLV", files, ffmpeg, ffprobe) }) { Text("FLV") }
                                DropdownMenuItem(onClick = { convertTo("GIF", files, ffmpeg, ffprobe) }) { Text("GIF") }
                            }
                        }
                    }
                }
                Button(onClick = { mode = Mode.IMPORT }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)) {
                    Text("Back")
                }
            }
        }
    }
}

fun convertTo(s: String, files: Set<File>, ffmpeg: FFmpeg, ffprobe: FFprobe) {
    files.forEach { file ->
        val executor = FFmpegExecutor(ffmpeg, ffprobe)
        val `in` = ffprobe.probe(file.path)

        val builder = FFmpegBuilder()
            .setInput(`in`)
            .addOutput("${file.name.split(".")[0]}.$s")
            .done()
        val job = executor.createJob(builder, object : ProgressListener {
            val duration_ns = `in`.getFormat().duration * TimeUnit.SECONDS.toNanos(1)
            override fun progress(progress: Progress) {
                val percentage = progress.out_time_ns / duration_ns
                println("${(percentage.toFloat() * 100).toInt()}% done")
            }
        })
        job.run()
    }
}


fun openFileDialog(
    window: ComposeWindow,
    title: String,
    allowedExtensions: List<String>,
    allowMultiSelection: Boolean = true
): Set<File> {
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
    }.files.toSet()
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
