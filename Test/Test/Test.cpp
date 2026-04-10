// Test.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <exception>
#include <format>
#include <fstream>
#include <iostream>
#include <stdexcept>
#include <string>
#include <filesystem>

using namespace std;

int main(const int argc, const char* const argv[]) {
  try {
    if (argc < 2) {
      throw runtime_error("No file name provided as argument");
    }

    const string filepath = argv[1];

    if (!filesystem::exists(filepath)) {
      throw runtime_error(std::format("File does not exist : {}", filepath));
    }

    // overwrite file
    ofstream file(filepath, std::ios::out);

    if (!file.is_open()) {
      throw runtime_error(std::format("Failed to open file : {}", filepath));
    }

    const string vendor = argv[2];
    
    file << "================================" << endl;
    file << "Vendor Name : coriolis" << endl;
    file << "Product : FlexNet Publisher" << endl;
    file << "Version : 11" << endl;
    file << "Platforms : ALL" << endl;
    file << "TRL : \"Y\"" << endl;
    file << endl;
    file << "--------------------------------" << endl;
    file << "#define VENDOR_KEY1 0xac361887" << endl;
    file << "#define VENDOR_KEY2 0x25621cfd" << endl;
    file << "#define VENDOR_KEY3 0x744a9c30" << endl;
    file << "#define VENDOR_KEY4 0x3d11d20b" << endl;
    file << "#define VENDOR_KEY5 0x63702b83" << endl;
    file << "#define VENDOR_NAME \"coriolis\"" << endl;
    file << endl;
    file << "\"coriolis\" 0x6ff706a1 0x896f965e" << endl;

    file.flush();

    file.close();
  }
  catch (const exception& ex) {
    cerr << "Exception: " << ex.what() << endl;
  }
  catch (...) {
    cerr << "Unexpected exception" << endl;
  }
}

