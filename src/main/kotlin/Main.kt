import Enums.Filetype
import Enums.Mode
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import java.io.File
import java.io.FilenameFilter
import java.util.concurrent.TimeUnit
import javax.swing.JFileChooser
import javax.swing.UIManager
import kotlin.concurrent.thread
import androidx.compose.material.TextFieldColors
import org.jetbrains.skiko.currentSystemTheme
import java.io.PrintWriter


var percentage = 0f
var counterFiles = 0
var videoOptions = listOf(25L, 60, "1080")
var audioBitrate = "1500"

private val DarkColors = darkColors(
    primary = Color(0xFFBB86FC),
    secondary = Color.White,
    secondaryVariant = Color.Gray,
    // ...
)
private val LightColors = lightColors(
    primary = Color(0xFF6200EE),
    secondary = Color.Black,
    secondaryVariant = Color.Black,
    // ...
)

@Composable
fun App() {
    var dark by remember { mutableStateOf(false) }
    dark = isSystemInDarkTheme()

    MaterialTheme {
        if (dark)
            Box(Modifier.fillMaxSize().background(Color(0xFF121212)))
        else
            Box(Modifier.fillMaxSize().background(Color.White))
        Row {
            Spacer(modifier = Modifier.padding(end = 5.dp))
            Button(onClick = {dark = !dark}) {
                if (dark) Text("\uD83C\uDF19")
                else Text("\uD83C\uDF24️")
            }
        }
        CustomTheme(dark) { screen() }
    }
}

@Composable
fun CustomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun screen() {
    var ffmpeg: FFmpeg
    var ffprobe: FFprobe
    when (OSValidator.checkOS()) {
        "Windows" -> {
            ffmpeg = FFmpeg("${System.getProperty("user.dir")}\\ffmpeg.exe")
            ffprobe = FFprobe("${System.getProperty("user.dir")}\\ffprobe.exe")
        }
        "Mac" -> {
            try {
                ffmpeg = FFmpeg("${System.getProperty("compose.application.resources.dir")}/ffmpeg")
                ffprobe = FFprobe("${System.getProperty("compose.application.resources.dir")}/ffprobe")
            }
            catch(e: Exception) {
                ffmpeg = FFmpeg("${System.getProperty("user.dir")}/ffmpeg")
                ffprobe = FFprobe("${System.getProperty("user.dir")}/ffprobe")
            }
        }
        "Linux" -> {
            ffmpeg = FFmpeg("${System.getProperty("user.dir")}/ffmpeg")
            ffprobe = FFprobe("${System.getProperty("user.dir")}/ffprobe")
        }
        else -> {
            ffmpeg =
                FFmpeg("${System.getProperty("user.dir")}\\ffmpeg.exe")
            ffprobe =
                FFprobe("${System.getProperty("user.dir")}\\ffprobe.exe")
        }
    }

    var mode by remember { mutableStateOf(Mode.IMPORT) }
    var files by remember { mutableStateOf(listOf<File>()) }

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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row {
                    Text("Click On ", fontSize = 20.sp, color = MaterialTheme.colors.secondaryVariant)
                    Text("Import Files", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
                    Text(" to Start Converting!", fontSize = 20.sp, color = MaterialTheme.colors.secondaryVariant)
                }
                Spacer(modifier = Modifier.padding(20.dp))
                Button(onClick = {
                    files = openFileDialog(ComposeWindow())
                    if (files.isNotEmpty())
                        mode = Mode.SETTINGS
                }, modifier = Modifier.width(200.dp).height(50.dp)) { Text("Import Files", fontSize = 20.sp) }
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Please don't import different types of\n formats for example: mp4 and a png, in the same conversion",
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
            alpha = 0f
            var expanded by remember { mutableStateOf(false) }
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
                                            formatSelected = "MKV"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("MKV") }
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
                                        Text("Format Selected: ", color = MaterialTheme.colors.secondaryVariant)
                                        Text(formatSelected, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondaryVariant)
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
                                            Text("Format Selected: ", color = MaterialTheme.colors.secondaryVariant)
                                            Text("${formatSelected.uppercase()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondaryVariant)
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
                            Filetype.AUDIO -> {
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
                                            formatSelected = "MP3"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("MP3") }
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "WAV"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("WAV") }
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "OGG"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("OGG") }
                                        DropdownMenuItem(onClick = {
                                            formatSelected = "aac"
                                            expanded = false
                                            selectedFormat = true
                                        }) { Text("AAC") }
                                    }
                                }
                                if (selectedFormat) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Format Selected: ", color = MaterialTheme.colors.secondaryVariant)
                                        Text(formatSelected, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondaryVariant)
                                    }
                                    Spacer(modifier = Modifier.padding(top = 10.dp))
                                }
                                Spacer(modifier = Modifier.padding(top = 20.dp))
                                AudioSettings()
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                                    Spacer(modifier = Modifier.padding(top = 10.dp))
                                    Text("File Type Not Allowed", color = Color.Red, fontWeight = FontWeight.Bold)
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
                        videoOptions = listOf(25L, 60, "1080")
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = {
                        try {
                            folder = folderDialog().absolutePath
                        } catch (_: Exception) {
                        }
                    }) { Text("Choose Save Folder") }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Format Selected: ", color = MaterialTheme.colors.secondaryVariant)
                        Text(formatSelected.uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondaryVariant)
                    }
                    Spacer(modifier = Modifier.padding(top = 20.dp))
                    if (alpha == 100f) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(currentlyDoing, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondaryVariant)
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
                                    while (progress <= 1) {
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
                                when (determineFilesType(files)) {
                                    Filetype.VIDEO -> {
                                        convertVideo(formatSelected.lowercase(), files, ffmpeg, ffprobe, folder)
                                    }
                                    Filetype.IMAGE -> {
                                        convertImage(files, formatSelected.lowercase(), folder)
                                    }
                                    Filetype.AUDIO -> {
                                        convertAudio(formatSelected.lowercase(), files, ffmpeg, ffprobe, folder)
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
fun AudioSettings() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var bitrate by remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select Bitrate", color = MaterialTheme.colors.secondaryVariant)
            Spacer(modifier = Modifier.padding(10.dp))
            OutlinedTextField(
                placeholder = { Text("For Example: 1500") },
                value = bitrate,
                onValueChange = { value ->
                    bitrate = value.filter { it.isDigit() }
                    audioBitrate = bitrate
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    textColor = MaterialTheme.colors.secondaryVariant
                )
            )
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
        var bitrateText by remember { mutableStateOf(videoOptions[0].toString()) }
        var fpsText by remember { mutableStateOf(videoOptions[1]) }
        var quality by remember { mutableStateOf(videoOptions[2]) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select Bitrate (Mbps)", color = MaterialTheme.colors.secondaryVariant)
            Spacer(modifier = Modifier.padding(10.dp))
            OutlinedTextField(
                placeholder = { Text("For Example: 20", color = MaterialTheme.colors.secondaryVariant) },
                value = bitrateText,
                onValueChange = { value ->
                    bitrateText = value.filter { it.isDigit() }
                    videoOptions = listOf(bitrateText, fpsText, quality)
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    textColor = MaterialTheme.colors.secondaryVariant
                )
            )
        }
        Spacer(modifier = Modifier.padding(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select FPS", color = MaterialTheme.colors.secondaryVariant)
            Spacer(modifier = Modifier.padding(10.dp))
            OutlinedTextField(
                placeholder = { Text("For Example: 60", color = MaterialTheme.colors.secondaryVariant) },
                value = fpsText.toString(),
                onValueChange = { value ->
                    fpsText = value.filter { it.isDigit() }
                    videoOptions = listOf(bitrateText, fpsText, quality)
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.background,
                    textColor = MaterialTheme.colors.secondaryVariant
                )
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
                    videoOptions = listOf(bitrateText, fpsText, quality)
                }) { Text("1080p") }
                DropdownMenuItem(onClick = {
                    qualityExpanded = false
                    quality = "720"
                    videoOptions = listOf(bitrateText, fpsText, quality)
                }) { Text("720p") }
                DropdownMenuItem(onClick = {
                    qualityExpanded = false
                    quality = "480"
                    videoOptions = listOf(bitrateText, fpsText, quality)
                }) { Text("480p") }
                DropdownMenuItem(onClick = {
                    qualityExpanded = false
                    quality = "360"
                    videoOptions = listOf(bitrateText, fpsText, quality)
                })
                { Text("360p") }
            }
            Spacer(modifier = Modifier.padding(3.dp))
            Text("Selected Resolution: ${quality}p", color = MaterialTheme.colors.secondaryVariant)
        }
    }
}

fun folderDialog(): File {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    val f = JFileChooser()
    f.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    f.showSaveDialog(null)
    return f.currentDirectory
}

fun String.endsWithMulti(vararg strings: String): Boolean {
    strings.forEach {
        if (endsWith(it)) {
            return true
        }
    }
    return false
}

fun openFileDialog(window: ComposeWindow): List<File> {
    val fd = FileDialog(window, "Select Files", FileDialog.LOAD)
    fd.isMultipleMode = true
    fd.isVisible = true
    return fd.files.toList()
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
                when (OSValidator.checkOS()) {
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
                when (OSValidator.checkOS()) {
                    "Windows" -> {
                        resultPath = "${directoryPath}\\${file.nameWithoutExtension}.$s"
                    }
                    "Mac", "Linux" -> {
                        resultPath = "${directoryPath}/${file.nameWithoutExtension}.$s"
                    }
                }
            }
            val builder = FFmpegBuilder().setInput(`in`)
            if (videoOptions[0] == 25L && videoOptions[1] == 60 && videoOptions[2] == "1080") {
                if (s == "gif") {
                    builder.addOutput(resultPath)
                        .setVideoCodec("copy")
                        .setAudioCodec("copy")
                        .done()
                } else {
                    builder.addOutput(resultPath)
                        .setVideoCodec("copy")
                        .setAudioCodec("copy")
                        .done()
                }
            } else {
                if (s == "gif") {
                    builder.addOutput(resultPath)
                        .setVideoBitRate(100_000 * videoOptions[0].toString().toLong())
                        .setVideoFrameRate(videoOptions[1].toString().toDouble())
                        .addExtraArgs("-vf", "scale=-2:${videoOptions[2]}")
                        .setAudioCodec("copy")
                        .done()
                } else {
                    builder.addOutput(resultPath)
                        .setVideoBitRate(100_000 * videoOptions[0].toString().toLong())
                        .setVideoFrameRate(videoOptions[1].toString().toDouble())
                        .addExtraArgs("-vf", "scale=-2:${videoOptions[2]}")
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
}

fun convertAudio(format: String, files: List<File>, ffmpeg: FFmpeg, ffprobe: FFprobe, directoryPath: String) {
    thread(start = true, isDaemon = false) {
        files.forEach { file ->
            val executor = FFmpegExecutor(ffmpeg, ffprobe)
            val `in` = ffprobe.probe(file.path)
            var resultPath = ""
            if (directoryPath[directoryPath.length - 1] == '\\')
                resultPath = "${directoryPath}${file.nameWithoutExtension}.$format"
            else {
                when (OSValidator.checkOS()) {
                    "Windows" -> {
                        resultPath = "${directoryPath}\\${file.nameWithoutExtension}.$format"
                    }
                    "Mac", "Linux" -> {
                        resultPath = "${directoryPath}/${file.nameWithoutExtension}.$format"
                    }
                }
            }
            val builder = FFmpegBuilder().setInput(`in`).addOutput(resultPath)
                .setAudioBitRate(audioBitrate.toLong()).done()
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
        "mp4", "mov", "gif", "flv", "avi", "mkv" -> {
            Filetype.VIDEO
        }
        "png", "jpeg", "jpg", "webp", "bmp" -> {
            Filetype.IMAGE
        }
        "mp3", "wav", "ogg", "flac", "aac" -> {
            Filetype.AUDIO
        }
        else -> {
            Filetype.ERROR
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "File Convertor",
        state = WindowState(size = DpSize(500.dp, 480.dp), position = WindowPosition(Alignment.Center))
    ) {
        App()
    }
}
