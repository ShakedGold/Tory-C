import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.imageio.ImageIO

object ImageConverter {
    @Throws(IOException::class)
    fun convertFormat(inputImagePath: String?, outputImagePath: String?, formatName: String?): Boolean {
        val inputStream = FileInputStream(inputImagePath)
        val outputStream = FileOutputStream(outputImagePath)

        // reads input image from file
        val inputImage = ImageIO.read(inputStream)

        // writes to the output image in specified format
        val result = ImageIO.write(inputImage, formatName, outputStream)

        // needs to close the streams
        outputStream.close()
        inputStream.close()
        return result
    }
}