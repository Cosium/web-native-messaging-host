import {test} from './fixtures';
import { expect } from 'earljs'

test('Connection should work', async ({backgroundPage}) => {
    await backgroundPage.evaluate(`nativeChannel.connect()`);
});

test('Posting message should work', async ({backgroundPage}) => {
    await backgroundPage.evaluate(`nativeChannel.connect()`);
    const messagePromise = backgroundPage.evaluate(`nativeChannel.pollOneMessage(1000)`);
    const message = {yo: 'man'};
    await backgroundPage.evaluate(`nativeChannel.port.postMessage(${JSON.stringify(message)})`);
    const echoedMessage = await messagePromise;

    expect(echoedMessage).toEqual(message);
});
