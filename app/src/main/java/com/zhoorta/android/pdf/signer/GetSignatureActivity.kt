package com.zhoorta.android.pdf.signer

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.github.gcacace.signaturepad.views.SignaturePad
import com.zhoorta.android.pdf.signer.pdftools.PDFTools
import kotlinx.android.synthetic.main.activity_get_signature.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class GetSignatureActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
    }
    private var mSignaturePad: SignaturePad? = null
    private var mClearButton: Button? = null
    private var mSaveButton: Button? = null
    private var config: SharedPreferences? = null
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_signature)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        config = getSharedPreferences("SignerConfig", 0)

        signature_pad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                Toast.makeText(this@GetSignatureActivity, "OnStartSigning", Toast.LENGTH_SHORT).show()
            }

            override fun onSigned() {
                save_button!!.isEnabled = true
                clear_button!!.isEnabled = true
            }

            override fun onClear() {
                save_button!!.isEnabled = false
                clear_button!!.isEnabled = false
            }
        })

        clear_button.setOnClickListener(View.OnClickListener { signature_pad.clear() })
        save_button.setOnClickListener(View.OnClickListener {
            Toast.makeText(this, resources.getString(R.string.info_saving_document), Toast.LENGTH_SHORT).show()
            val xpos = config!!.getString("xpos", "110").toInt()
            val ypos = config!!.getString("ypos", "170").toInt()
            val width = config!!.getString("width", "200").toInt()
            val height = config!!.getString("height", "50").toInt()
            val page = config!!.getString("page", "1").toInt()
            val pdf = PDFTools(applicationContext)
            val signatureBitmap = signature_pad!!.getTransparentSignatureBitmap()
            val outputSignature = saveSignature(signatureBitmap)

            var  tmpOrigin : File =  File(getIntent().getExtras().getString("file"));
            var tmpLocalFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/destino.pdf")

            //create the file
            try {
                tmpOrigin.copyTo(tmpLocalFile, true, 4096)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            pdf.open(tmpLocalFile)
            pdf.insertImage(outputSignature!!.path, xpos, ypos, width, height, page)

            /*
                new SMBServerConnect(new SMBServerConnect.AsyncResponse(){
                    @Override
                    public void processFinish(final SMBTools smb, String error){

                        if(smb==null) { alert(error); return;}

                        try {
                            int xpos = Integer.parseInt(config.getString("xpos", "110"));
                            int ypos = Integer.parseInt(config.getString("ypos", "170"));
                            int width = Integer.parseInt(config.getString("width", "200"));
                            int height = Integer.parseInt(config.getString("height", "50"));
                            int page = Integer.parseInt(config.getString("page", "1"));


                            PDFTools pdf = new PDFTools(getApplicationContext());
                            Bitmap signatureBitmap = mSignaturePad.getTransparentSignatureBitmap();
                            final File outputSignature = saveSignature(signatureBitmap);

                            final File tmpLocalFile = new File(getIntent().getExtras().getString("file"));
                            pdf.open(tmpLocalFile);
                            pdf.insertImage(outputSignature.getPath(),xpos,ypos,width,height,page);



                            new SMBCopyLocalFile(smb, GetSignatureActivity.this.config.getString("outboundPath", "out") + "\\" + getIntent().getExtras().getString("source"), new SMBCopyLocalFile.AsyncResponse() {
                                @Override
                                public void processFinish(boolean success, String error) {

                                    //tmpLocalFile.delete();
                                    outputSignature.delete();

                                    new SMBDeleteRemoteFile(smb, new SMBDeleteRemoteFile.AsyncResponse() {
                                        @Override
                                        public void processFinish(boolean success, String error) {


                                            smb.close();

                                            finish();

                                            Intent intent = new Intent(GetSignatureActivity.this, ShowFinalDocumentActivity.class);
                                            intent.putExtra("file", tmpLocalFile.getAbsolutePath());

                                            startActivity(intent);

                                        }
                                    }).execute(GetSignatureActivity.this.config.getString("inboundPath", "in") + "\\" + getIntent().getExtras().getString("source"));

                                }
                            }).execute(tmpLocalFile);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }).execute(getApplicationContext());*/
            val intent = Intent(this@GetSignatureActivity, MainActivity::class.java)
            startActivity(intent);
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.size <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@GetSignatureActivity, "Cannot write images to external storage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun saveBitmapToPNG(bitmap: Bitmap, photo: File) {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        val stream: OutputStream = FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
    }

    // Donde se guarda la firma para meterla al documento
    private fun saveSignature(signature: Bitmap): File? {
        try {
            val output = File.createTempFile("signer", ".png", applicationContext.cacheDir)
            saveBitmapToPNG(signature, output)
            return output
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


}