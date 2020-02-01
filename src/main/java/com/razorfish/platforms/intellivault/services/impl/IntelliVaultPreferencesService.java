package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
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

    private static final Logger log = Logger.getInstance(IntelliVaultPreferencesService.class);

    public IntelliVaultPreferences getPreferences() {
        if (preferences == null) {
            preferences = new IntelliVaultPreferences();
        }

        if (preferences.repoConfigList == null || preferences.repoConfigList.size() == 0) {
            preferences.repoConfigList = preferences.getDefaultRepos();
        }

        return (IntelliVaultPreferences) preferences.clone();
    }

    public void setPreferences(IntelliVaultPreferences preferences) {
        this.preferences = preferences;
    }

    private void storeCredentials(String repoName, Credentials credentials) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(repoName);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    private Credentials retrieveCredentials(String repoName) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(repoName);

        return PasswordSafe.getInstance().get(credentialAttributes);
    }

    private CredentialAttributes createCredentialAttributes(String repoName) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName(IntelliVaultConstants.CREDENTIAL_STORE_SUBSYSTEM, repoName));
    }

    @Nullable
    @Override
    public IntelliVaultPreferences getState() {
        IntelliVaultPreferences preferences = getPreferences();

        final Set<String> newRepoNames = new HashSet<>();
        preferences.getRepoConfigList().forEach(repo -> {
            String name = repo.getName();
            newRepoNames.add(name);

            String username = repo.getUsername();
            String password = repo.getPassword();

            Credentials credentials = new Credentials(username, password);
            storeCredentials(name, credentials);

            repo.setUsername(null);
            repo.setPassword(null);
        });

        //get the previous repo names, then remove new ones, leaving only those that were deleted
        if (this.preferences != null && this.preferences.getRepoConfigList() != null) {
            Set<String> previousRepoNames = this.preferences.getRepoConfigList().stream().map(IntelliVaultCRXRepository::getName).collect(Collectors.toSet());
            previousRepoNames.removeAll(newRepoNames);

            for (String repoName : previousRepoNames) {
                //set null to remove
                storeCredentials(repoName, null);
            }
        }

        return preferences;
    }

    @Override
    public void loadState(IntelliVaultPreferences preferences) {
        preferences.getRepoConfigList().forEach(repo -> {
            String name = repo.getName();

            Credentials credentials = retrieveCredentials(name);
            if (credentials != null) {
                String username = credentials.getUserName();
                String password = credentials.getPasswordAsString();

                repo.setUsername(username);
                repo.setPassword(password);
            } else {
                log.error("Unable to retrieve credentials for repository: " + name);
            }
        });

        setPreferences(preferences);
    }
}
