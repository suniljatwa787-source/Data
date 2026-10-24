package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.RecordRepository
import com.example.util.RichTextFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DocuGrid", appName)
  }

  @Test
  fun `test rich text stats calculation`() {
    val sampleText = "# Title\nThis is a sample document notes section with **bold** words."
    val stats = RichTextFormatter.calculateStats(sampleText)
    assertTrue(stats.first >= 9) // word count
    assertTrue(stats.second > 20) // char count
  }

  @Test
  fun `test attachment converter`() {
    val sampleAtt = com.example.data.model.RecordAttachment(
      fileName = "spec.pdf",
      mimeType = "application/pdf",
      fileSizeBytes = 2048,
      localPath = "/data/files/spec.pdf"
    )
    val json = com.example.data.model.AttachmentConverter.attachmentsToJson(listOf(sampleAtt))
    val parsed = com.example.data.model.AttachmentConverter.jsonToAttachments(json)
    assertEquals(1, parsed.size)
    assertEquals("spec.pdf", parsed[0].fileName)
    assertTrue(parsed[0].isPdf)
  }
}
