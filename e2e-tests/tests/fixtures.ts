import {type BrowserContext, chromium, Page, test as base} from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';
import * as os from 'os';

export const test = base.extend<{
    userDataDir: string;
    context: BrowserContext;
    extensionId: string;
    testAppExecutableDir: string;
    backgroundPage: Page;
}>({
    userDataDir: async ({}, use) => {
        const userDataDir = fs.mkdtempSync(path.join(os.tmpdir(), 'user-data-dir-'));
        await use(userDataDir);
        fs.rmSync(userDataDir, {recursive: true});
    },
    context: async ({userDataDir}, use) => {
        const pathToExtension = path.join(__dirname, '..', 'web-extension');

        const context = await chromium.launchPersistentContext(userDataDir, {
            headless: false,
            args: [
                `--disable-extensions-except=${pathToExtension}`,
                `--load-extension=${pathToExtension}`,
            ],
        });
        await use(context);
        await context.close();
    },
    extensionId: async ({context}, use) => {
        let [background] = context.backgroundPages()
        if (!background) {
            background = await context.waitForEvent('backgroundpage');
        }

        const extensionId = background.url().split('/')[2];
        await use(extensionId);
    },
    testAppExecutableDir: async ({userDataDir, extensionId}, use) => {
        const testAppExecutableDir = fs.mkdtempSync(path.join(os.tmpdir(), 'test-app-'));
        await use(testAppExecutableDir);
        fs.rmSync(testAppExecutableDir, {recursive: true});
    },
    backgroundPage: async ({page, extensionId}, use) => {
        await page.goto(`chrome-extension://${extensionId}/background.html`);
        await page.reload();
        await use(page);
    }
});

test.beforeAll(({page}) => {
    page.on('console', msg => console.log(`[Page] ${msg.text()}`));
});

test.beforeAll(({userDataDir, extensionId, testAppExecutableDir}) => {
    const targetDir = path.join(__dirname, '..', 'target');

    const jarPath = path.join(targetDir, 'test-app.jar');
    const env = JSON.parse(fs.readFileSync(path.join(targetDir, 'env.json'), 'utf-8'));
    const javaHome = env['java.home'];
    const javaCommand = `${javaHome}/bin/java -jar ${jarPath}`;

    const testAppExecutablePath = path.join(testAppExecutableDir, 'test-app.sh');
    const testAppShell = `
        #!/usr/bin/env bash
        ${javaCommand}
        `;
    fs.writeFileSync(testAppExecutablePath, testAppShell);
    fs.chmodSync(testAppExecutablePath, 0o755);

    const hostManifest = `
        {
          "name": "com.cosium.web_native_messaging_host_test",
          "description": "foo foo",
          "path": "${testAppExecutablePath}",
          "type": "stdio",
          "allowed_origins": [
            "chrome-extension://${extensionId}/"
          ]
        }
        `;

    const nativeMessagingHostDir = path.join(userDataDir, 'NativeMessagingHosts');
    fs.mkdirSync(nativeMessagingHostDir);
    fs.writeFileSync(
        path.join(nativeMessagingHostDir, 'com.cosium.web_native_messaging_host_test.json'),
        hostManifest
    );
});

export const expect = test.expect;
