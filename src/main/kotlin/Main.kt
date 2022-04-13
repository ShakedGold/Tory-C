import Enums.Filetype
import Enums.Mode
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.text.style.TextAlign
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
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.progress.Progress
import net.bramp.ffmpeg.progress.ProgressListener
import java.awt.FileDialog
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.swing.JFileChooser
import javax.swing.UIManager
import kotlin.concurrent.thread


var percentage = 0f
var counterFiles = 0
var options = listOf(25L, 60, "1080")

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
    var selectedFormat by remember { mutableStateOf(false) }
    var formatSelected by remember { mutableStateOf("") }
    var alpha by remember { mutableStateOf(0f) }
    var currentlyDoing by remember { mutableStateOf("Finished Converting 0 Files") }
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    ).value
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
                            ffmpeg = FFmpeg("${System.getProperty("user.dir")}\\ffmpegFiles\\Windows\\ffmpeg")
                            ffprobe = FFprobe("${System.getProperty("user.dir")}\\ffmpegFiles\\Windows\\ffprobe")
                        }
                        "Mac" -> {
                            ffmpeg = FFmpeg("${System.getProperty("user.dir")}/ffmpegFiles/Mac/ffmpeg")
                            ffprobe = FFprobe("${System.getProperty("user.dir")}/ffmpegFiles/Mac/ffprobe")
                        }
                        "Linux" -> {
                            ffmpeg = FFmpeg("${System.getProperty("user.dir")}/ffmpegFiles/Linux/ffmpeg")
                            ffprobe = FFprobe("${System.getProperty("user.dir")}/ffmpegFiles/Linux/ffprobe")
                        }
                        else -> {
                            ffmpeg =
                                FFmpeg("${System.getProperty("user.dir")}\\ffmpegFiles\\Windows\\ffmpeg")
                            ffprobe =
                                FFprobe("${System.getProperty("user.dir")}\\ffmpegFiles\\Windows\\ffprobe")
                        }
                    }
                    files = openFileDialog(
                        ComposeWindow(),
                        "Choose Files",
                        listOf("mov", "mp4", "flv", "avi", "png", "jpeg", "gif", "jpg"),
                        true
                    )
                    if (files.isNotEmpty())
                        mode = Mode.SETTINGS
                }) { Text("Import Files") }
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Please don't import different types of formats for example: mp4 and a png, in the same conversion",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 40.dp)) {
                    RadioButton(onClick = {}, selected = true, enabled = false)
                    RadioButton(onClick = {}, selected = false, enabled = false)
                    RadioButton(onClick = {}, selected = false, enabled = false)
                }
            }
        }
        Mode.SETTINGS -> {
            var expanded by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                LazyColumn(Modifier.weight(1f)) {
                    item {
                        when (determineFilesType(files)) {
                            Filetype.VIDEO -> {
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().padding(5.dp)
                                ) { Text("Choose Format") }
                                Column {
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "MOV"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("MOV") }
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "MP4"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("MP4") }
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "AVI"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("AVI") }
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "FLV"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("FLV") }
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "GIF"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("GIF") }
                                    }
                                }
                                if (selectedFormat) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Format Selected: ")
                                        Text(formatSelected, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.padding(top = 10.dp))
                                }
                                Spacer(modifier = Modifier.padding(top = 20.dp))
                                VideoSettings()
                            }
                            Filetype.IMAGE -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (selectedFormat) {
                                        Row {
                                            Text("Format Selected: ")
                                            Text("${formatSelected.uppercase()}", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Button(onClick = {
                                        formatSelected = "png"
                                        selectedFormat = true
                                    }) { Text("Convert To PNG") }
                                    Button(onClick = {
                                        formatSelected = "jpg"
                                        selectedFormat = true
                                    }) { Text("Convert To JPG") }
                                    Button(onClick = {
                                        formatSelected = "jpeg"
                                        selectedFormat = true
                                    }) { Text("Convert To JPEG") }
                                    Button(onClick = {
                                        formatSelected = "bmp"
                                        selectedFormat = true
                                    }) { Text("Convert To BMP") }
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 40.dp)) {
                    RadioButton(onClick = {}, selected = false, enabled = false)
                    RadioButton(onClick = {}, selected = true, enabled = false)
                    RadioButton(onClick = {}, selected = false, enabled = false)
                }
                //Controls
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        mode = Mode.IMPORT
                        selectedFormat = false
                        formatSelected = ""
                        currentlyDoing = "Finished Converting $counterFiles Files"
                        alpha = 0f
                        options = listOf(25L, 60, "1080")
                    }) { Text("Back") }
                    var enabled by remember { mutableStateOf(false) }
                    if (formatSelected.isNotEmpty()) enabled = true
                    Button(onClick = {
                        mode = Mode.FOLDER_SELECT
                    }, modifier = Modifier.padding(20.dp), enabled = enabled) { Text("Next") }
                }
            }
        }
        Mode.FOLDER_SELECT -> {
            var folder = ""
            var backEnabled by remember { mutableStateOf(true) }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = {
                        try {
                            folder = folderDialog().path
                        } catch (_: Exception) { }
                    }) { Text("Choose Save Folder") }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Format Selected: ")
                        Text(formatSelected.uppercase(), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.padding(top = 20.dp))
                    if (alpha == 100f) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(currentlyDoing, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = animatedProgress,
                                modifier = Modifier.fillMaxWidth().size(10.dp).alpha(alpha)
                            )
                        }
                    }
                }
                //Controls
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 40.dp)) {
                        RadioButton(onClick = {}, selected = false, enabled = false)
                        RadioButton(onClick = {}, selected = false, enabled = false)
                        RadioButton(onClick = {}, selected = true, enabled = false)
                    }
                    Row(modifier = Modifier.weight(1f, false)) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = {
                                mode = Mode.SETTINGS
                                progress = 0f
                                counterFiles = 0
                                currentlyDoing = "Finished Converting $counterFiles Files"
                            }, enabled = backEnabled) { Text("Back") }
                            Button(onClick = {
                                thread(start = true, isDaemon = false) {
                                    progress = 0f
                                    while(progress <= 1) {
                                        progress = percentage
                                        currentlyDoing = "Finished Converting $counterFiles Files"
                                        if (files.size == counterFiles) {
                                            currentlyDoing = "Finished Converting ${files.size} Files"
                                            backEnabled = true
                                            progress = 1f
                                            break
                                        }
                                    }
                                }
                                alpha = 100f
                                when(determineFilesType(files)) {
                                    Filetype.VIDEO -> {
                                        convertVideo(formatSelected.lowercase(), files, ffmpeg, ffprobe, folder)
                                    }
                                    Filetype.IMAGE -> {
                                        convertImage(files, formatSelected.lowercase(), folder)
                                    }
                                    else -> {}
                                }
                                backEnabled = false
                            }, modifier = Modifier.padding(20.dp)) { Text("Start Converting") }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun VideoSettings() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var bitrateText by remember { mutableStateOf(options[0].toString()) }
        var fpsText by remember { mutableStateOf(options[1]) }
        var quality by remember { mutableStateOf(options[2]) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select Bitrate (Mbps)")
            Spacer(modifier = Modifier.padding(10.dp))
            OutlinedTextField(
                placeholder = { Text("For Example: 20") },
                value = bitrateText,
                onValueChange = { value ->
                    bitrateText = value.filter { it.isDigit() }
                    options = listOf(bitrateText, fpsText, quality)
                }
            )
        }
        Spacer(modifier = Modifier.padding(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select FPS")
            Spacer(modifier = Modifier.padding(10.dp))
            OutlinedTextField(
                placeholder = { Text("For Example: 60") },
                value = fpsText.toString(),
                onValueChange = { value ->
                    fpsText = value.filter { it.isDigit() }
                    options = listOf(bitrateText, fpsText, quality)
                }
            )
        }
        Spacer(modifier = Modifier.padding(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            var qualityExpanded by remember { mutableStateOf(false) }
            Button(onClick = { qualityExpanded = true }) { Text("Select Resolution") }
            DropdownMenu(expanded = qualityExpanded, onDismissRequest = { qualityExpanded = !qualityExpanded }) {
                DropdownMenuItem(onClick = {
                    qualityExpanded = false
                    quality = "1080"
                    options = listOf(bitrateText, fpsText, quality)
                }) { Text("1080p") }
                DropdownMenuItem(onClick = {
                    qualityExpanded = false
                    quality = "720"
                    options = listOf(bitrateText, fpsText, quality)
                }) { Text("720p") }
                DropdownMenuItem(onClick = {
                    qualityExpanded = false
                    quality = "480"
                    options = listOf(bitrateText, fpsText, quality)
                }) { Text("480p") }
                DropdownMenuItem(onClick = {
                    qualityExpanded = false
                    quality = "360"
                    options = listOf(bitrateText, fpsText, quality)
                })
                { Text("360p") }
            }
            Spacer(modifier = Modifier.padding(3.dp))
            Text("Selected Resolution: ${quality}p")
        }
    }
}

fun folderDialog(): File {
    when (OSValidator.checkOS()) {
        "Windows" -> {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
        }
        "Mac" -> {}
        "Linux" -> {}
        else -> {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
        }
    }
    val f = JFileChooser()
    f.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    f.showSaveDialog(null)
    return f.selectedFile
}
fun openFileDialog(window: ComposeWindow, title: String, allowedExtensions: List<String>, allowMultiSelection: Boolean = true): List<File> {
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

fun convertImage(files: List<File>, format: String, directory: String) {
    thread(start = true, isDaemon = false) {
        val filesSize = files.size
        var counter = 0
        var resultPath = ""
        files.forEach { file ->
            if (directory[directory.length - 1] == '\\')
                resultPath = "${directory}${file.nameWithoutExtension}.$format"
            else {
                when(OSValidator.checkOS()) {
                    "Windows" -> {
                        resultPath = "${directory}\\${file.nameWithoutExtension}.$format"
                    }
                    "Mac", "Linux" -> {
                        resultPath = "${directory}/${file.nameWithoutExtension}.$format"
                    }
                }
            }
            ImageConverter.convertFormat(file.path, "$resultPath", format.uppercase())
            counter++
            percentage = counter.toFloat() / filesSize.toFloat()
            counterFiles++
        }
    }
}
fun convertVideo(s: String, files: List<File>, ffmpeg: FFmpeg, ffprobe: FFprobe, directoryPath: String) {
    thread(start = true, isDaemon = false) {
        files.forEach { file ->
            val executor = FFmpegExecutor(ffmpeg, ffprobe)
            val `in` = ffprobe.probe(file.path)
            var resultPath = ""
            if (directoryPath[directoryPath.length - 1] == '\\')
                resultPath = "${directoryPath}${file.nameWithoutExtension}.$s"
            else {
                when(OSValidator.checkOS()) {
                    "Windows" -> {
                        resultPath = "${directoryPath}\\${file.nameWithoutExtension}.$s"
                    }
                    "Mac", "Linux" -> {
                        resultPath = "${directoryPath}/${file.nameWithoutExtension}.$s"
                    }
                }
            }
            val builder = FFmpegBuilder().setInput(`in`)
            if (s == "gif") {
                builder.addOutput(resultPath)
                    .setVideoBitRate(100_000 * options[0].toString().toLong())
                    .setVideoFrameRate(options[1].toString().toDouble())
                    .addExtraArgs("-vf", "scale=-2:${options[2]}")
                    .setAudioCodec("copy")
                    .done()
            } else {
                builder.addOutput(resultPath)
                    .setVideoBitRate(100_000 * options[0].toString().toLong())
                    .setVideoFrameRate(options[1].toString().toDouble())
                    .addExtraArgs("-vf", "scale=-2:${options[2]}")
                    .setVideoCodec("h264")
                    .setAudioCodec("copy")
                    .done()
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
fun determineFileType(fileExt: String): Filetype {
    return when (fileExt.lowercase()) {
        "mp4", "mov", "gif", "flv", "avi" -> {
            Filetype.VIDEO
        }
        "png", "jpeg", "jpg", "webp", "bmp" -> {
            Filetype.IMAGE
        }
        else -> {
            Filetype.ERROR
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "File Convertor", state = WindowState(size = DpSize(500.dp, 480.dp), position = WindowPosition(Alignment.Center))) {
        App()
    }
}