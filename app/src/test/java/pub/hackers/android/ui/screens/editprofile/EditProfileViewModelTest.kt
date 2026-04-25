package pub.hackers.android.ui.screens.editprofile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pub.hackers.android.data.repository.HackersPubRepository
import pub.hackers.android.domain.model.EditableAccount
import pub.hackers.android.domain.model.EditableAccountLink
import pub.hackers.android.testutil.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<HackersPubRepository>(relaxed = true)

    private val sampleAccount = EditableAccount(
        id = "account-1",
        name = "Alice",
        bio = "original bio",
        avatarUrl = "https://example.com/avatar.png",
        handle = "@alice@hackers.pub",
        links = listOf(
            EditableAccountLink(name = "Blog", url = "https://blog.example"),
        ),
    )

    private fun stubLoad(result: EditableAccount = sampleAccount) {
        coEvery { repository.getEditableAccount() } returns Result.success(result)
    }

    @Test
    fun `init loads account and populates state`() = runTest {
        stubLoad()
        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("account-1", state.id)
        assertEquals("Alice", state.name)
        assertEquals("original bio", state.bio)
        assertEquals("https://example.com/avatar.png", state.avatarUrl)
        assertEquals(1, state.links.size)
        assertEquals(false, state.isLoading)
        assertNull(state.loadError)
    }

    @Test
    fun `load failure stores loadError`() = runTest {
        coEvery { repository.getEditableAccount() } returns
            Result.failure(RuntimeException("not signed in"))
        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()

        assertEquals("not signed in", vm.uiState.value.loadError)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `edits update state`() = runTest {
        stubLoad()
        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()

        vm.onNameChange("Alice B")
        vm.onBioChange("new bio")
        vm.onAvatarPicked("data:image/jpeg;base64,xxx")
        vm.onLinkAdd()
        vm.onLinkChange(1, "GitHub", "https://github.com/alice")

        val state = vm.uiState.value
        assertEquals("Alice B", state.name)
        assertEquals("new bio", state.bio)
        assertEquals("data:image/jpeg;base64,xxx", state.pendingAvatarDataUrl)
        assertEquals(2, state.links.size)
        assertEquals("GitHub", state.links[1].name)
    }

    @Test
    fun `onLinkRemove drops the link at index`() = runTest {
        stubLoad()
        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()

        vm.onLinkRemove(0)

        assertTrue(vm.uiState.value.links.isEmpty())
    }

    @Test
    fun `save emits Saved event on success`() = runTest {
        stubLoad()
        coEvery {
            repository.updateAccount(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()

        vm.save()
        advanceUntilIdle()

        val event = vm.events.first()
        assertEquals(EditProfileEvent.Saved, event)
        assertEquals(false, vm.uiState.value.isSaving)
        assertNull(vm.uiState.value.saveError)
    }

    @Test
    fun `save omits avatarUrl when none picked`() = runTest {
        stubLoad()
        coEvery {
            repository.updateAccount(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateAccount(
                id = "account-1",
                name = "Alice",
                bio = "original bio",
                avatarUrl = null,
                links = any(),
            )
        }
    }

    @Test
    fun `save passes picked avatar data URL through`() = runTest {
        stubLoad()
        coEvery {
            repository.updateAccount(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()
        vm.onAvatarPicked("data:image/jpeg;base64,abc")
        vm.save()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateAccount(
                id = any(),
                name = any(),
                bio = any(),
                avatarUrl = "data:image/jpeg;base64,abc",
                links = any(),
            )
        }
    }

    @Test
    fun `save drops blank links before sending`() = runTest {
        stubLoad(sampleAccount.copy(links = emptyList()))
        coEvery {
            repository.updateAccount(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()

        vm.onLinkAdd()
        vm.onLinkChange(0, "  ", "  ")
        vm.onLinkAdd()
        vm.onLinkChange(1, "Site", "https://site.example")
        vm.save()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateAccount(
                id = any(),
                name = any(),
                bio = any(),
                avatarUrl = any(),
                links = listOf(EditableAccountLink("Site", "https://site.example")),
            )
        }
    }

    @Test
    fun `save failure populates saveError and clears isSaving`() = runTest {
        stubLoad()
        coEvery {
            repository.updateAccount(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("server exploded"))

        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()

        assertEquals("server exploded", vm.uiState.value.saveError)
        assertEquals(false, vm.uiState.value.isSaving)
    }

    @Test
    fun `dismissSaveError clears saveError`() = runTest {
        stubLoad()
        coEvery {
            repository.updateAccount(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("x"))
        val vm = EditProfileViewModel(repository)
        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()

        vm.dismissSaveError()

        assertNull(vm.uiState.value.saveError)
    }
}
