import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import java.awt.FileDialog
import java.io.File

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
    var files by remember { mutableStateOf(setOf<File>()) }

    Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Row {
            Text("Click On ", fontSize = 20.sp)
            Text("Import Files", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(" to Start Converting!", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.padding(20.dp))
        Button(onClick = {
            val ffmpeg = FFmpeg( "src/main/kotlin/FFmpeg Files/ffmpeg")
            val ffprobe = FFprobe("src/main/kotlin/FFmpeg Files/ffprobe", func)
            files = openFileDialog(ComposeWindow(), "Select File", listOf())

            files.forEach { file ->
                val builder = FFmpegBuilder().setInput(file.path).addOutput("/Result/output.mp4").done()
                val excecutor = FFmpegExecutor(ffmpeg, ffprobe)
                excecutor.createJob(builder).run()
            }

        }) { Text("Import Files") }
    }
}
fun openFileDialog(window: ComposeWindow, title: String, allowedExtensions: List<String>, allowMultiSelection: Boolean = true): Set<File> {
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
    Window(onCloseRequest = ::exitApplication, title = "File Convertor", state = WindowState(size = DpSize(430.dp, 400.dp), position = WindowPosition(Alignment.Center))) {
        App()
    }
}
