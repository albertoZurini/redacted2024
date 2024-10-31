import { expect } from "chai";
import { ethers } from "hardhat";

describe("PointOfSale Contract", function () {
    let PointOfSale, pos, owner, addr1, addr2, ERC20Token, token;

    beforeEach(async function () {
        // Get contract factory and signers
        PointOfSale = await ethers.getContractFactory("PointOfSale");
        ERC20Token = await ethers.getContractFactory("ERC20Mock");
        [owner, addr1, addr2] = await ethers.getSigners();

        // Deploy the PointOfSale contract
        pos = await PointOfSale.deploy();
        await pos.deployed();

        // Deploy an ERC20 token for testing
        token = await ERC20Token.deploy("Test Token", "TTK", 100000);
        await token.deployed();
        
        // Whitelist the ERC20 token in the PointOfSale contract
        await pos.whitelistToken(token.address);
    });

    it("Should receive Ether and log an order", async function () {
        await addr1.sendTransaction({ to: pos.address, value: ethers.utils.parseEther("1") });
        const order = await pos.getOrder(0);
        expect(order.sender).to.equal(addr1.address);
        expect(order.amount).to.equal(ethers.utils.parseEther("1"));
        expect(order.isERC20).to.be.false;
    });

    it("Should whitelist an ERC20 token and allow payment with it", async function () {
        await token.connect(addr1).approve(pos.address, 1000);
        await pos.connect(addr1).payWithERC20(token.address, 1000);
        const order = await pos.getOrder(0);
        expect(order.sender).to.equal(addr1.address);
        expect(order.amount).to.equal(1000);
        expect(order.isERC20).to.be.true;
        expect(order.tokenAddress).to.equal(token.address);
    });

    it("Should reject payments with non-whitelisted ERC20 tokens", async function () {
        const nonWhitelistedToken = await ERC20Token.deploy("Another Token", "ATK", 100000);
        await nonWhitelistedToken.deployed();
        
        await expect(pos.connect(addr1).payWithERC20(nonWhitelistedToken.address, 1000)).to.be.revertedWith("Token not whitelisted");
    });

    it("Should allow the owner to withdraw Ether after one week", async function () {
        await addr1.sendTransaction({ to: pos.address, value: ethers.utils.parseEther("1") });
        
        // Move forward in time by one week
        await ethers.provider.send("evm_increaseTime", [7 * 24 * 60 * 60]);
        await ethers.provider.send("evm_mine", []);

        const balanceBefore = await ethers.provider.getBalance(owner.address);
        await pos.connect(owner).batchWithdraw([0]);
        const balanceAfter = await ethers.provider.getBalance(owner.address);

        expect(balanceAfter).to.be.above(balanceBefore);
    });

    it("Should allow the owner to withdraw ERC20 tokens after one week", async function () {
        await token.connect(addr1).approve(pos.address, 1000);
        await pos.connect(addr1).payWithERC20(token.address, 1000);

        await ethers.provider.send("evm_increaseTime", [7 * 24 * 60 * 60]);
        await ethers.provider.send("evm_mine", []);

        await expect(() => pos.connect(owner).batchWithdraw([0])).to.changeTokenBalance(token, owner, 1000);
    });

    it("Should process batch returns with specified percentages", async function () {
        await addr1.sendTransaction({ to: pos.address, value: ethers.utils.parseEther("1") });
        
        // Approve and pay with ERC20 token as addr2
        await token.connect(addr2).approve(pos.address, 1000);
        await pos.connect(addr2).payWithERC20(token.address, 1000);

        // Process batch return of 50% for each order
        await pos.connect(owner).batchReturn([0, 1], [50, 50]);

        const etherOrder = await pos.getOrder(0);
        const tokenOrder = await pos.getOrder(1);

        expect(etherOrder.refunded).to.be.true;
        expect(tokenOrder.refunded).to.be.true;

        // Check that refunds were correctly processed
        expect(await ethers.provider.getBalance(addr1.address)).to.be.above(ethers.utils.parseEther("0.5"));
        expect(await token.balanceOf(addr2.address)).to.be.above(500);
    });

    it("Should restrict whitelisting functions to the owner", async function () {
        await expect(pos.connect(addr1).whitelistToken(token.address)).to.be.revertedWith("Not the contract owner");
        await expect(pos.connect(addr1).removeTokenFromWhitelist(token.address)).to.be.revertedWith("Not the contract owner");
    });
});
