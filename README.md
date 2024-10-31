# Ne-ar-yo: NFC Blockchain Payment Demo Application

## Project Overview

**Ne-ar-yo** is a cutting-edge Android application that integrates Near Field Communication (NFC) technology with blockchain to provide a seamless and secure tap-to-pay experience. This application is designed to empower users with a decentralized method of conducting transactions using multiple blockchain networks, making it an ideal solution for modern point-of-sale (PoS) systems.

### Vision

Our vision is to democratize payment processing in the blockchain space, allowing users to transact easily and securely without relying on traditional banking systems or intermediaries. By harnessing the capabilities of NFC technology, Ne-ar-yo aims to create a frictionless payment experience, facilitating the widespread adoption of cryptocurrencies in everyday transactions.

### Key Features

1. **NFC Reader**:
   - The NFC Reader activity allows users to easily read NFC tags that contain blockchain transaction information. This feature can be used to interact with various digital assets and cryptocurrency wallets by simply tapping the device on compatible NFC tags.
   - The app processes incoming NFC data efficiently, displaying relevant transaction details such as payment amounts and associated addresses in a user-friendly interface.
   - Users can easily verify the authenticity of transactions and ensure security by reviewing the displayed information before proceeding.

2. **Host Card Emulation (HCE)**:
   - The HCE activity transforms the application into a virtual NFC card, allowing users to set and manage blockchain transaction messages for easy transmission when tapped against an NFC reader.
   - Users can customize their messages and choose which blockchain or wallet address they wish to transact with, ensuring flexibility and convenience.
   - This functionality is designed specifically for PoS systems, enabling businesses to accept cryptocurrency payments effortlessly, reducing the need for specialized hardware, and making transactions accessible to everyone.

3. **Multi-Chain Support**:
   - Ne-ar-yo is built with a focus on multi-chain compatibility, enabling users to transact across various blockchain networks. This feature enhances the versatility of the application and allows users to utilize the blockchain of their choice for payments.
   - Users can switch between supported blockchains, facilitating transactions based on user preferences or specific use cases.

### Use Cases

- **Blockchain Payments**: 
  - Enable instant and secure cryptocurrency transactions, allowing users to tap and pay using their mobile devices.
  - Provide a modern payment solution for businesses looking to accept cryptocurrencies without complicated setups.

- **PoS Integration**:
  - Allow retailers to adopt cryptocurrency payment systems, streamlining their operations and expanding their payment options.
  - Reduce reliance on traditional payment processors, allowing businesses to keep more of their earnings.

- **Event Ticketing**:
  - Facilitate contactless ticketing for events and concerts, allowing attendees to use their digital wallets to purchase and validate tickets via NFC.

## Tech Stack

- **Programming Language**: Kotlin
- **Frameworks**: Android SDK, Jetpack Compose for modern UI development
- **Libraries**: N/A (custom implementation for NFC and blockchain functionalities)
- **Blockchain Integration**: Support for various chains, including Ethereum and NEAR Protocol

## Getting Started

To get started with the Ne-ar-yo NFC Blockchain Payment Demo Application, follow these steps:

### Prerequisites

- **Android Studio**: Ensure you have Android Studio installed on your machine. You can download it from [here](https://developer.android.com/studio).
- **Physical Device**: NFC functionality is not supported on emulators; use a physical Android device with NFC capability.
- **Enable NFC**: Make sure that NFC is enabled on your device in the settings.

### Installation Steps

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/ne-ar-yo.git
   cd ne-ar-yo
   ```

2. **Open the Project**:
   - Launch Android Studio and select "Open an Existing Project".
   - Navigate to the cloned directory and open the project.

3. **Sync Gradle**:
   - Allow Android Studio to sync the project dependencies. This may take a moment.

4. **Run the App**:
   - Connect your physical device to your computer via USB.
   - Ensure that USB debugging is enabled on your device.
   - Click the "Run" button in Android Studio to install the app on your device.

5. **Explore the App**:
   - Once the app is installed, open it on your device and navigate through the NFC Reader and HCE functionalities to test blockchain transactions.

Certainly! Here’s the new section you can add to the README file that explains how to connect the ACR122u NFC reader to a phone using the Scriptor tool for exchanging data using APDU commands.

---

### How to Connect ACR122u to Phone Using Scriptor

To facilitate communication between your phone and the ACR122u NFC reader using APDU commands, follow these steps:

1. **Connect the ACR122u**
   - Use a USB cable to connect the ACR122u NFC reader to your computer. Ensure that the device is powered on and recognized by your operating system.

2. **Open `Scriptor`**
   - Launch the `Scriptor` application on your computer. This tool allows you to send APDU commands to the NFC reader and interact with your application.

3. **Instantiate the Handshake with the App**
   - In the `Scriptor` interface, enter the following command to initiate a handshake with the application running on your mobile device:
     ```
     00A4040007F0394148148100
     ```
     - **Note**: Here, `F0394148148100` is the Application Identifier (AID) that your app uses to identify itself during the communication process.

4. **Start a Read Binary Command**
   - After successfully establishing a handshake, you can read data from the NFC tag by sending the following command:
     ```
     00B00000FF
     ```
     - This command requests to read binary data from the tag, starting at the specified offset.

### Additional Notes
- Ensure that your mobile application is set up to handle these commands and that it is running on your device.
- Make sure that the NFC functionality is enabled on your mobile device for successful communication.
- You can monitor the response from the ACR122u in the `Scriptor` interface to verify that the commands are being executed correctly.

---

Feel free to add this section to your README to provide clear instructions for users on how to connect the ACR122u with your application using the Scriptor tool.

## Future Work

We plan to expand the capabilities of Ne-ar-yo with additional features, including:

- **Integration with More Blockchains**: Adding support for more blockchain networks to broaden user choices.
- **Enhanced Security Features**: Implementing advanced security protocols to protect user data and transactions.
- **User Customization Options**: Allowing users to customize their transaction experience, including themes and preferred settings.

## Contribution

We welcome contributions to the Ne-ar-yo NFC Blockchain Payment Demo Application! If you would like to contribute, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them.
4. Push your changes to your forked repository.
5. Submit a pull request describing your changes and why they should be merged.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Acknowledgements

We would like to thank the open-source community for their contributions, as well as the developers and enthusiasts who inspire and support blockchain innovations.