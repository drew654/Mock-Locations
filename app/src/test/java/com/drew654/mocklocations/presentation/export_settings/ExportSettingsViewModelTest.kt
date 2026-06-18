package com.drew654.mocklocations.presentation.export_settings

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.widget.Toast
import com.drew654.mocklocations.MainDispatcherRule
import com.drew654.mocklocations.domain.SettingsManager
import com.drew654.mocklocations.domain.model.LocationTarget
import com.drew654.mocklocations.repository.ExportRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.OutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ExportSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application: Application = mockk(relaxed = true)
    private val settingsManager: SettingsManager = mockk(relaxed = true)
    private val exportRepository: ExportRepository = mockk()
    private val contentResolver: ContentResolver = mockk()
    private val mockToast: Toast = mockk(relaxed = true)
    
    private lateinit var viewModel: ExportSettingsViewModel

    private val mockRoutes = listOf(
        mockk<LocationTarget.SavedRoute>(),
        mockk<LocationTarget.SavedRoute>()
    )

    @Before
    fun setUp() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.testDispatcher
        
        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<String>(), any()) } returns mockToast
        
        every { settingsManager.savedRoutesFlow } returns flowOf(mockRoutes)
        every { application.contentResolver } returns contentResolver
        
        viewModel = ExportSettingsViewModel(application, settingsManager, exportRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
        unmockkStatic(Toast::class)
    }

    @Test
    fun `init sets initial state correctly when routes exist`() = runTest {
        val state = viewModel.state.value
        assertEquals(2, state.routesToExport)
        assertTrue(state.isExportSettings)
        assertTrue(state.isExportRoutes)
    }

    @Test
    fun `init sets initial state correctly when no routes exist`() = runTest {
        every { settingsManager.savedRoutesFlow } returns flowOf(emptyList())
        
        val viewModel = ExportSettingsViewModel(application, settingsManager, exportRepository)
        
        val state = viewModel.state.value
        assertEquals(0, state.routesToExport)
        assertTrue(state.isExportSettings)
        assertFalse(state.isExportRoutes)
    }

    @Test
    fun `setIsExportSettings updates state`() {
        viewModel.setIsExportSettings(true)
        assertTrue(viewModel.state.value.isExportSettings)

        viewModel.setIsExportSettings(false)
        assertFalse(viewModel.state.value.isExportSettings)
    }

    @Test
    fun `setIsExportRoutes updates state`() {
        viewModel.setIsExportRoutes(true)
        assertTrue(viewModel.state.value.isExportRoutes)

        viewModel.setIsExportRoutes(false)
        assertFalse(viewModel.state.value.isExportRoutes)
    }

    @Test
    fun `exportDataToUri successfully exports data`() = runTest {
        val uri = mockk<Uri>()
        val jsonString = "{\"test\": \"data\"}"
        val outputStream = mockk<OutputStream>(relaxed = true)

        coEvery { exportRepository.generateExportToJson(application, any(), any()) } returns jsonString
        every { contentResolver.openOutputStream(uri) } returns outputStream

        viewModel.exportDataToUri(uri)
        
        advanceUntilIdle()

        coVerify { exportRepository.generateExportToJson(application, true, true) }
        verify { outputStream.write(jsonString.toByteArray()) }
        verify { outputStream.flush() }
        verify { Toast.makeText(application, "Export successful", Toast.LENGTH_SHORT) }
        verify { mockToast.show() }
    }

    @Test
    fun `exportDataToUri shows error Toast when export fails`() = runTest {
        val uri = mockk<Uri>()
        coEvery { exportRepository.generateExportToJson(application, any(), any()) } throws Exception("Test exception")

        viewModel.exportDataToUri(uri)
        
        advanceUntilIdle()

        verify { Toast.makeText(application, "Export failed", Toast.LENGTH_SHORT) }
        verify { mockToast.show() }
    }
    
    @Test
    fun `exportDataToUri shows error Toast when output stream fails`() = runTest {
        val uri = mockk<Uri>()
        coEvery { exportRepository.generateExportToJson(application, any(), any()) } returns "{}"
        every { contentResolver.openOutputStream(uri) } throws Exception("IO failure")

        viewModel.exportDataToUri(uri)
        
        advanceUntilIdle()

        verify { Toast.makeText(application, "Export failed", Toast.LENGTH_SHORT) }
        verify { mockToast.show() }
    }
}
