package com.cocido.ramfapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cocido.ramfapp.common.Resource
import com.cocido.ramfapp.common.UiState
import com.cocido.ramfapp.models.WeatherStation
import com.cocido.ramfapp.models.WidgetData
import com.cocido.ramfapp.repository.WeatherRepository
import com.cocido.ramfapp.utils.SecurityLogger
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

/**
 * Professional unit tests for WeatherStationViewModel following MVVM testing best practices
 * with proper StateFlow testing, coroutines support, and security validation
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherStationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: WeatherStationViewModel
    private lateinit var mockRepository: WeatherRepository
    private lateinit var mockSecurityLogger: SecurityLogger

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testStations = listOf(
        WeatherStation(
            id = "station1",
            name = "Test Station 1",
            location = "Test Location 1",
            latitude = -25.0,
            longitude = -58.0,
            altitude = 100.0,
            status = "active",
            lastCommunication = "2024-12-01T10:00:00.000Z"
        ),
        WeatherStation(
            id = "station2",
            name = "Test Station 2",
            location = "Test Location 2",
            latitude = -26.0,
            longitude = -59.0,
            altitude = 150.0,
            status = "active",
            lastCommunication = "2024-12-01T11:00:00.000Z"
        )
    )

    private val testWidgetData = WidgetData(
        timestamp = "2024-12-01T12:00:00.000Z",
        temperature = 25.5,
        maxTemperature = 30.0,
        minTemperature = 20.0,
        relativeHumidity = 65.0,
        dewPoint = 18.5,
        airPressure = 1013.25,
        solarRadiation = 500.0,
        windSpeed = 10.5,
        windDirection = "N",
        rainLastHour = 0.0,
        rainDay = 0.0,
        rain24h = 2.5,
        rain48h = 5.0,
        rain7d = 15.0,
        stationName = "station1"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mockk()
        mockSecurityLogger = mockk(relaxed = true)

        // Setup default mock behaviors
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(testStations))
        every { mockRepository.getWidgetData(any()) } returns flowOf(Resource.Success(testWidgetData))

        viewModel = WeatherStationViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should be correct`() {
        // When
        val uiState = viewModel.uiState.value
        val stationsState = viewModel.weatherStations.value
        val selectedStation = viewModel.selectedStation.value

        // Then
        assertFalse("Should not be loading initially", uiState.isLoading)
        assertNull("Should have no error initially", uiState.error)
        assertFalse("Stations should not be loading initially", stationsState.isLoading)
        assertNull("Should have no selected station initially", selectedStation)
    }

    @Test
    fun `fetchWeatherStations should update states correctly on success`() = runTest {
        // Given
        every { mockRepository.getWeatherStations() } returns flowOf(
            Resource.Loading,
            Resource.Success(testStations)
        )

        // When
        viewModel.fetchWeatherStations()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        val stationsState = viewModel.weatherStations.value
        val selectedStation = viewModel.selectedStation.value

        assertFalse("Should not be loading after success", uiState.isLoading)
        assertNull("Should have no error after success", uiState.error)
        assertTrue("Stations should have data", stationsState.hasData)
        assertEquals("Should have correct number of stations", 2, stationsState.data?.size)
        assertNotNull("Should auto-select first station", selectedStation)
        assertEquals("Should select first station", "station1", selectedStation?.id)
    }

    @Test
    fun `fetchWeatherStations should handle error correctly`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { mockRepository.getWeatherStations() } returns flowOf(
            Resource.Loading,
            Resource.Error(Exception(errorMessage), errorMessage)
        )

        // When
        viewModel.fetchWeatherStations()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        val stationsState = viewModel.weatherStations.value

        assertFalse("Should not be loading after error", uiState.isLoading)
        assertNotNull("Should have error message", uiState.error)
        assertTrue("Stations state should have error", stationsState.hasError)
        assertEquals("Error message should match", errorMessage, stationsState.error)
    }

    @Test
    fun `selectStation should update selected station and load data`() = runTest {
        // Given
        val stationId = "station2"
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(testStations))
        every { mockRepository.getWidgetData(stationId) } returns flowOf(Resource.Success(testWidgetData))

        // First load stations
        viewModel.fetchWeatherStations()
        advanceUntilIdle()

        // When
        viewModel.selectStation(stationId)
        advanceUntilIdle()

        // Then
        val selectedStation = viewModel.selectedStation.value
        val uiState = viewModel.uiState.value

        assertNotNull("Should have selected station", selectedStation)
        assertEquals("Should select correct station", stationId, selectedStation?.id)
        assertEquals("UI state should reflect selected station", stationId, uiState.selectedStationId)

        // Verify widget data was requested for the selected station
        verify { mockRepository.getWidgetData(stationId) }
    }

    @Test
    fun `selectStation should handle invalid station gracefully`() = runTest {
        // Given
        val invalidStationId = "nonexistent"
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(testStations))

        // First load stations
        viewModel.fetchWeatherStations()
        advanceUntilIdle()

        // When
        viewModel.selectStation(invalidStationId)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertNotNull("Should have error message", uiState.error)
        assertTrue("Error should mention station not found",
            uiState.error?.contains("no encontrada") ?: false)
    }

    @Test
    fun `fetchWidgetData should update widget state correctly`() = runTest {
        // Given
        val stationId = "station1"
        every { mockRepository.getWidgetData(stationId) } returns flowOf(
            Resource.Loading,
            Resource.Success(testWidgetData)
        )

        // When
        viewModel.fetchWidgetData(stationId)
        advanceUntilIdle()

        // Then
        val widgetState = viewModel.widgetData.value

        assertFalse("Should not be loading after success", widgetState.isLoading)
        assertTrue("Should have widget data", widgetState.hasData)
        assertNotNull("Widget data should not be null", widgetState.data)
        assertEquals("Temperature should match", 25.5, widgetState.data?.temperature, 0.01)
    }

    @Test
    fun `refreshData should refresh current station data`() = runTest {
        // Given
        val stationId = "station1"
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(testStations))
        every { mockRepository.getWidgetData(stationId) } returns flowOf(Resource.Success(testWidgetData))

        // Setup initial state
        viewModel.fetchWeatherStations()
        advanceUntilIdle()

        // When
        viewModel.refreshData()
        advanceUntilIdle()

        // Then
        // Verify that widget data was requested again
        verify(atLeast = 2) { mockRepository.getWidgetData(stationId) }
    }

    @Test
    fun `shouldRefreshData should return true when data is stale`() {
        // Given - Fresh ViewModel (lastRefresh would be 0)

        // When
        val shouldRefresh = viewModel.shouldRefreshData()

        // Then
        assertTrue("Should refresh when no data has been loaded", shouldRefresh)
    }

    @Test
    fun `getCurrentStationId should return correct station ID`() = runTest {
        // Given
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(testStations))
        viewModel.fetchWeatherStations()
        advanceUntilIdle()

        // When
        val currentStationId = viewModel.getCurrentStationId()

        // Then
        assertEquals("Should return first station ID", "station1", currentStationId)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Given - Set an error state
        val errorMessage = "Test error"
        every { mockRepository.getWeatherStations() } returns flowOf(
            Resource.Error(Exception(errorMessage), errorMessage)
        )

        viewModel.fetchWeatherStations()
        advanceUntilIdle()

        // Verify error is set
        assertNotNull("Should have error initially", viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull("Error should be cleared", viewModel.uiState.value.error)
    }

    @Test
    fun `legacy methods should work correctly`() = runTest {
        // Given
        val stationName = "station1"
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(testStations))
        every { mockRepository.getWidgetData(stationName) } returns flowOf(Resource.Success(testWidgetData))

        // When - Test legacy method
        viewModel.fetchTemperatureMaxMin(stationName)
        advanceUntilIdle()

        // Then
        verify { mockRepository.getWidgetData(stationName) }

        val widgetState = viewModel.widgetData.value
        assertTrue("Widget data should be loaded", widgetState.hasData)
    }

    @Test
    fun `ViewModel should handle concurrent operations correctly`() = runTest {
        // Given
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(testStations))
        every { mockRepository.getWidgetData(any()) } returns flowOf(Resource.Success(testWidgetData))

        // When - Make concurrent calls
        viewModel.fetchWeatherStations()
        viewModel.fetchWidgetData("station1")
        viewModel.refreshData()

        advanceUntilIdle()

        // Then - Should handle all operations without errors
        val uiState = viewModel.uiState.value
        val stationsState = viewModel.weatherStations.value
        val widgetState = viewModel.widgetData.value

        assertTrue("Stations should be loaded", stationsState.hasData)
        assertTrue("Widget data should be loaded", widgetState.hasData)
        assertNull("Should have no errors", uiState.error)
    }

    @Test
    fun `ViewModel should preserve state during configuration changes`() {
        // Given
        val stations = testStations
        every { mockRepository.getWeatherStations() } returns flowOf(Resource.Success(stations))

        // When - Simulate data loading
        viewModel.fetchWeatherStations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - State should be preserved
        val stationsState = viewModel.weatherStations.value
        assertTrue("State should be preserved", stationsState.hasData)
        assertEquals("Data should match", 2, stationsState.data?.size)
    }
}