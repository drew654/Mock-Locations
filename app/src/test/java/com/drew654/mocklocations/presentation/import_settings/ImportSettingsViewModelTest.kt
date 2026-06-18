package com.drew654.mocklocations.presentation.import_settings

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.drew654.mocklocations.BuildConfig
import com.drew654.mocklocations.MainDispatcherRule
import com.drew654.mocklocations.domain.model.ImportRouteOption
import com.drew654.mocklocations.repository.ExportRepository
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application: Application = mockk(relaxed = true)
    private val exportRepository: ExportRepository = mockk()
    private val mockToast: Toast = mockk(relaxed = true)

    private lateinit var viewModel: ImportSettingsViewModel

    @Before
    fun setUp() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.testDispatcher
        every { Dispatchers.Default } returns mainDispatcherRule.testDispatcher

        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<String>(), any()) } returns mockToast

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        viewModel = ImportSettingsViewModel(application, exportRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
        unmockkStatic(Toast::class)
        unmockkStatic(Log::class)
    }

    @Test
    fun `setImportUri updates state and refreshes summary on success`() = runTest {
        val uri = mockk<Uri>()
        val json = "{\"test\": \"json\"}"
        coEvery { exportRepository.readJsonFromUri(application, uri) } returns json
        every { exportRepository.getVersionCodeFromJson(json) } returns BuildConfig.VERSION_CODE
        every { exportRepository.isWithSettingsToImport(json) } returns true
        every { exportRepository.getRouteCountFromJson(json) } returns 5

        viewModel.setImportUri(uri)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(uri, state.importUri)
        assertTrue(state.isImportRoutesEnabled)
        assertTrue(state.isImportRoutes)
        assertTrue(state.isImportSettingsEnabled)
        assertTrue(state.isImportSettings)
        assertEquals(ImportRouteOption.REPLACE, state.importRouteOption)
        assertEquals(5, state.routesToImport)
    }

    @Test
    fun `setImportUri with null uri only updates state`() = runTest {
        viewModel.setImportUri(null)
        
        val state = viewModel.state.value
        assertNull(state.importUri)
        verify { exportRepository wasNot Called }
    }

    @Test
    fun `setImportUri shows error toast if version code is higher`() = runTest {
        val uri = mockk<Uri>()
        val json = "{\"test\": \"json\"}"
        coEvery { exportRepository.readJsonFromUri(application, uri) } returns json
        every { exportRepository.getVersionCodeFromJson(json) } returns (BuildConfig.VERSION_CODE + 1)

        viewModel.setImportUri(uri)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.importUri)
        verify { Toast.makeText(application, "App version is out of date", Toast.LENGTH_SHORT) }
        verify { mockToast.show() }
    }

    @Test
    fun `setImportUri handles exception when reading json`() = runTest {
        val uri = mockk<Uri>()
        coEvery { exportRepository.readJsonFromUri(application, uri) } throws Exception("Read failed")

        viewModel.setImportUri(uri)
        advanceUntilIdle()

        verify { Toast.makeText(application, "Failed to read file", Toast.LENGTH_SHORT) }
        verify { mockToast.show() }
    }

    @Test
    fun `setIsImportRoutes updates state`() {
        viewModel.setIsImportRoutes(true)
        assertTrue(viewModel.state.value.isImportRoutes)
        
        viewModel.setIsImportRoutes(false)
        assertFalse(viewModel.state.value.isImportRoutes)
    }

    @Test
    fun `setIsImportSettings updates state`() {
        viewModel.setIsImportSettings(true)
        assertTrue(viewModel.state.value.isImportSettings)
        
        viewModel.setIsImportSettings(false)
        assertFalse(viewModel.state.value.isImportSettings)
    }

    @Test
    fun `setImportRouteOption updates state`() {
        viewModel.setImportRouteOption(ImportRouteOption.MERGE)
        assertEquals(ImportRouteOption.MERGE, viewModel.state.value.importRouteOption)
        
        viewModel.setImportRouteOption(ImportRouteOption.REPLACE)
        assertEquals(ImportRouteOption.REPLACE, viewModel.state.value.importRouteOption)
    }

    @Test
    fun `importDataFromUri successfully imports data`() = runTest {
        val uri = mockk<Uri>()
        val json = "{\"test\": \"json\"}"
        val onSuccess: () -> Unit = mockk(relaxed = true)

        coEvery { exportRepository.readJsonFromUri(application, uri) } returns json
        every { exportRepository.getVersionCodeFromJson(json) } returns BuildConfig.VERSION_CODE
        every { exportRepository.isWithSettingsToImport(json) } returns true
        every { exportRepository.getRouteCountFromJson(json) } returns 5
        
        viewModel.setImportUri(uri)
        advanceUntilIdle()

        coEvery { exportRepository.importFromJson(json, any(), any()) } returns Unit

        viewModel.importDataFromUri(onSuccess)
        advanceUntilIdle()

        coVerify { exportRepository.importFromJson(json, true, ImportRouteOption.REPLACE) }
        verify { Toast.makeText(application, "Import successful", Toast.LENGTH_SHORT) }
        verify { onSuccess() }
        assertFalse(viewModel.state.value.isImporting)
    }

    @Test
    fun `importDataFromUri handles failure`() = runTest {
        val uri = mockk<Uri>()
        val json = "{\"test\": \"json\"}"
        val onSuccess: () -> Unit = mockk(relaxed = true)

        coEvery { exportRepository.readJsonFromUri(application, uri) } returns json
        every { exportRepository.getVersionCodeFromJson(json) } returns BuildConfig.VERSION_CODE
        every { exportRepository.isWithSettingsToImport(json) } returns true
        every { exportRepository.getRouteCountFromJson(json) } returns 5
        
        viewModel.setImportUri(uri)
        advanceUntilIdle()

        coEvery { exportRepository.importFromJson(json, any(), any()) } throws Exception("Import failed")

        viewModel.importDataFromUri(onSuccess)
        advanceUntilIdle()

        verify { Toast.makeText(application, "Import failed", Toast.LENGTH_SHORT) }
        verify(exactly = 0) { onSuccess() }
        assertFalse(viewModel.state.value.isImporting)
    }

    @Test
    fun `importDataFromUri returns early if no uri`() = runTest {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        
        viewModel.importDataFromUri(onSuccess)
        
        verify { exportRepository wasNot Called }
        verify(exactly = 0) { onSuccess() }
    }
}
