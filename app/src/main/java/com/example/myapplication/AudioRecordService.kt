package com.example.myapplication
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.media.AudioManager

class AudioRecordService : Service() {

    private lateinit var audioRecord: AudioRecord
    private var isRecording = false
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
        audioFormat)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.startBluetoothSco()
        audioManager.isBluetoothScoOn = true

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )



        isRecording = true
        audioRecord.startRecording()

        Thread {
            try {
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                while (isRecording) {
                    val fileName = "audio_record_${System.currentTimeMillis()}.wav"
                    val file = File(downloadsDir, fileName)
                    val fos = FileOutputStream(file)

                    val totalAudioLen = sampleRate * 2 * 10 // 5 секунд * 2 байта на сэмпл (16 бит)
                    val totalDataLen = totalAudioLen + 36
                    val channels = 1
                    val byteRate = 16 * sampleRate * channels / 8

                    writeWavHeader(fos, totalAudioLen.toLong(), totalDataLen.toLong(),
                        sampleRate, channels, byteRate)

                    val buffer = ByteArray(bufferSize)
                    var bytesWritten = 0
                    while (bytesWritten < totalAudioLen && isRecording) {
                        val read = audioRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            fos.write(buffer, 0, read)
                            bytesWritten += read
                        }
                    }
                    fos.flush()
                    fos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                audioRecord.stop()
                audioRecord.release()
                stopSelf()
            }
        }.start()

        return START_STICKY
    }

    override fun onDestroy() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
        isRecording = false
        super.onDestroy()
    }



    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "audio_service_channel",
                "Audio Recording Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "audio_service_channel")
            .setContentTitle("Audio Recording")
            .setContentText("Recording audio in 5-second WAV files")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        sampleRate: Int,
        channels: Int,
        byteRate: Int
    ) {
        val header = ByteArray(44)

        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // file size minus 8 bytes
        writeInt(header, 4, (totalDataLen).toInt())

        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        // Subchunk1Size (16 for PCM)
        writeInt(header, 16, 16)

        // AudioFormat (1 for PCM)
        writeShort(header, 20, 1.toShort())

        // NumChannels
        writeShort(header, 22, channels.toShort())

        // SampleRate
        writeInt(header, 24, sampleRate)

        // ByteRate
        writeInt(header, 28, byteRate)

        // BlockAlign
        writeShort(header, 32, (channels * 16 / 8).toShort())

        // BitsPerSample
        writeShort(header, 34, 16.toShort())

        // data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        // Subchunk2Size
        writeInt(header, 40, totalAudioLen.toInt())

        out.write(header, 0, 44)
    }

    private fun writeInt(header: ByteArray, offset: Int, value: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = ((value shr 8) and 0xff).toByte()
        header[offset + 2] = ((value shr 16) and 0xff).toByte()
        header[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(header: ByteArray, offset: Int, value: Short) {
        header[offset] = (value.toInt() and 0xff).toByte()
        header[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }
}