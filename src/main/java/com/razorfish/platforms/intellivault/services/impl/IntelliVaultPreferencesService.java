package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import com.razorfish.platforms.intellivault.utils.IntelliVaultConstants;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The preferences services handles storing and retrieving the configuration state of the vault plugin.
 */
@State(name = "IntelliVaultPreferencesService", storages = {
        @Storage("IntelliVaultPreferencesService.xml")})
public class IntelliVaultPreferencesService implements PersistentStateComponent<IntelliVaultPreferences> {

    private IntelliVaultPreferences preferences;

    public IntelliVaultPreferences getPreferences() {
        if (preferences == null) {
            preferences = new IntelliVaultPreferences();
        }

        if (preferences.repoConfigList == null || preferences.repoConfigList.size() == 0) {
            preferences.repoConfigList = preferences.getDefaultRepos();
        }

        IntelliVaultPreferences clone = (IntelliVaultPreferences) preferences.clone();
        clone.getRepoConfigList().forEach(repo -> {
            String name = repo.getName();

            CredentialAttributes credentialAttributes = createCredentialAttributes(name);

            Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
            if (credentials != null) {
                String username = credentials.getUserName();
                String password = credentials.getPasswordAsString();

                repo.setUsername(username);
                repo.setPassword(password);
            } else {
                //todo log an error
            }
        });

        return clone;
    }

    public void setPreferences(IntelliVaultPreferences preferences) {
        final Set<String> newRepoNames = new HashSet<>();
        preferences.getRepoConfigList().forEach(repo -> {
            String name = repo.getName();
            newRepoNames.add(name);

            String username = repo.getUsername();
            String password = repo.getPassword();

            CredentialAttributes credentialAttributes = createCredentialAttributes(name);
            Credentials credentials = new Credentials(username, password);
            PasswordSafe.getInstance().set(credentialAttributes, credentials);

            repo.setUsername("username");
            repo.setPassword("password");
        });

        //get the previous repo names, then remove new ones, leaving only those that were deleted
        Set<String> previousRepoNames = this.preferences.getRepoConfigList().stream().map(IntelliVaultCRXRepository::getName).collect(Collectors.toSet());
        previousRepoNames.removeAll(newRepoNames);

        for (String repoName : previousRepoNames) {
            CredentialAttributes credentialAttributes = createCredentialAttributes(repoName);
            //set null to remove
            PasswordSafe.getInstance().set(credentialAttributes, null);
        }

        this.preferences = preferences;
    }

    private CredentialAttributes createCredentialAttributes(String repoName) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName(IntelliVaultConstants.CREDENTIAL_STORE_SUBSYSTEM, repoName));
    }

    @Nullable
    @Override
    public IntelliVaultPreferences getState() {
        return getPreferences();
    }

    @Override
    public void loadState(IntelliVaultPreferences preferences) {
        setPreferences(preferences);
    }
}
